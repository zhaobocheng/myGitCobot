package com.bop.web.bopmain.internal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcOperations;

import com.bop.domain.schema.ModuleService;
import com.bop.hibernate.dbinit.ExecutableUpgrader;
import com.bop.hibernate.dbinit.ModuleUpgrader;

public class UpgradeExecutor implements InitializingBean {
	private static Logger log = LoggerFactory.getLogger(UpgradeExecutor.class);
	private JdbcOperations jdbcTemplate;
	private ModuleService moduleService;
	private List<ModuleUpgrader> systemUpgraders = new ArrayList<ModuleUpgrader>();
	private static Map<String, ExecutableUpgrader[]> cachedUpgraders = null;
	
	public synchronized void onBind(ModuleUpgrader up, Map<?, ?> serviceProps) throws Exception {
		log.warn("bind a System ModuleUpgrater: " + up + ", serviceProps:" + serviceProps);
		this.systemUpgraders.add(up);
	}

	public synchronized void onUnbind(ModuleUpgrader up, Map<?, ?> serviceProps) throws Exception {
		log.warn("unbind a System ModuleUpgrater: " + up);
		this.systemUpgraders.remove(up);
		cachedUpgraders = null;
	}
	
	public void setModuleService(ModuleService moduleService) {
		this.moduleService = moduleService;
	}
	
	public boolean isNeedUpgrader() {
		return this.getModuleUpdaters().size() != 0;
	}
	
	/**
	 * 得到所有可升级的模块及升级脚本
	 * @return
	 */
	public Map<String, ExecutableUpgrader[]> getModuleUpdaters() {
		if(cachedUpgraders != null) return cachedUpgraders;
			
		cachedUpgraders = new LinkedHashMap<String, ExecutableUpgrader[]>();
		log.debug("处理系统升级");
		for(ModuleUpgrader mu : systemUpgraders) {
			log.debug("module:" + mu.getModuleName());
			ExecutableUpgrader[] us = this.getUpgraders(mu);
			if(us.length > 0) cachedUpgraders.put(mu.getModuleName(), us);
		}
		
		log.debug("处理系统升级完毕");
		
		log.debug("得到所有的升级");
		List<ModuleUpgrader> upgraders = this.moduleService.getUpgraders();
		
		log.debug("当前得到的升级服务");
		for(ModuleUpgrader mu : upgraders) {
			log.debug("module:" + mu.getModuleName());
 			ExecutableUpgrader[] us = this.getUpgraders(mu);
			if(us.length > 0) cachedUpgraders.put(mu.getModuleName(), us);
		}
		
		log.debug("处理系统升级结束，需升级的个数为" + cachedUpgraders.size());
		
		return cachedUpgraders;
	}
	
	public ExecutableUpgrader[] getModuleUpdaters(String moduleName) {
		return this.getModuleUpdaters().get(moduleName);
	}
	
	/**
	 * 根据给定的升级脚本和数据库中模块的当前版本，选择需要执行的升级脚本，并按顺序排好
	 * @param up
	 * @return
	 */
	public ExecutableUpgrader[] getUpgraders(ModuleUpgrader up) {
		String moduleName = up.getModuleName();
		if(up.getUpgraders().length == 0) return new ExecutableUpgrader[0];
		
		//运行起来的模块的版本
		Version moduleVersion = up.getModuleVersion();
		moduleVersion = this.versionFormat(moduleVersion);
		//模块中配置的最高的sql文件脚本
		Version heightVsersion = this.getHeihtVersion(up);
		if(heightVsersion == null) {
			log.info(moduleName + ":无升级脚本，不需要升级bundle!");
			return new ExecutableUpgrader[0];
		}
		heightVsersion = this.versionFormat(heightVsersion);
		
		//此模块在数据库中的当前版本
		Version dbVersion = this.getModuleVersion(moduleName);
		dbVersion = this.versionFormat(dbVersion);
		
		log.debug("数据库中的版本为：" + dbVersion);
		
		if(dbVersion.compareTo(heightVsersion)>0) {
			log.error(moduleName + ":数据库中的版本大于该Bundle的版本!未执行该Bundle的升级脚本!");
			return new ExecutableUpgrader[0];
		}
		
		//判断是否执行更新
		if(!dbVersion.equals(heightVsersion)){
			//选择要执行的升级脚本以及其顺序
			ExecutableUpgrader[] actionScript = this.selectScript(up, dbVersion, heightVsersion);
			return actionScript;
		}
		
		return new ExecutableUpgrader[0];
	}
	
	public void ExecuteUpgraders(String moduleName) {
		ExecutableUpgrader[] actionScript = this.getModuleUpdaters(moduleName);
		
		for (ExecutableUpgrader upgrader : actionScript) {
			log.warn("执行升级：" + upgrader);
			upgrader.execute(this.jdbcTemplate);
			this.updateVersion(moduleName, upgrader.getToVersion());
			log.warn("升级bundle（ "+ moduleName +" ）到版本（ "+ upgrader.getToVersion() +" ）");
		}
		
		cachedUpgraders.remove(moduleName);
	}
	
	/**
	 * 获得sql文件中的最高版本
	 */
	private Version getHeihtVersion(ModuleUpgrader up) {
		if(this.getFinalUpgraderSql(up) == null) {
			return null;
		}else {
			log.debug("数据库脚本能升级到的最高版本是" + this.getFinalUpgraderSql(up).getToVersion());
			return this.getFinalUpgraderSql(up).getToVersion();
		}
	}
	/**
	 * 获得最高版本文件对象
	 */
	private ExecutableUpgrader getFinalUpgraderSql(ModuleUpgrader up) {
		ExecutableUpgrader heightExecutableUpgrader = null;
		Version heightVersion = null;
		for (int i = 0; i<up.getUpgraders().length; i++) {
			
			ExecutableUpgrader u = up.getUpgraders()[i];
			if (u==null)
			{
				return null;
			}
			if(i == 0) {// 为第一个最高版本赋第一个值
				heightVersion = u.getToVersion();
				heightExecutableUpgrader = u;
			}
			if(u.getToVersion().compareTo(heightVersion) > 0) {// 比较找到最高的一个版本
				heightVersion = u.getToVersion();
				heightExecutableUpgrader = u;
			}
		}
		log.debug("最高版本执行文件是：（" + heightExecutableUpgrader.getFromVresion()+ ":"+ heightExecutableUpgrader.getToVersion() +" ）");
		return heightExecutableUpgrader;
	}
	
	/**
	 * 选择要升级的脚本
	 * @param up
	 * @param dbVersion
	 * @param moduleVersion
	 * @return
	 */
	private ExecutableUpgrader[] selectScript(ModuleUpgrader up, Version dbVersion, Version moduleVersion){
		LinkedHashMap<Version, LinkedHashMap<Version, ExecutableUpgrader>> upgraders = new LinkedHashMap<Version, LinkedHashMap<Version, ExecutableUpgrader>>();
		
		for(ExecutableUpgrader u : up.getUpgraders()) {
			Version toVersion = this.versionFormat(u.getToVersion());
			Version fromVersion = this.versionFormat(u.getFromVresion());
			
			//筛选脚本 : 文件中fromVersion>=数据库中dbVersion 
			if(fromVersion.compareTo(dbVersion)<0) continue;
			if(!upgraders.containsKey(toVersion)) {
				LinkedHashMap<Version, ExecutableUpgrader> temp = new LinkedHashMap<Version, ExecutableUpgrader>();
				temp.put(fromVersion, u);
				upgraders.put(toVersion, temp);
			} else {
				LinkedHashMap<Version, ExecutableUpgrader> temp = upgraders.get(toVersion);
				temp.put(fromVersion, u);
			}
		}
		log.debug("选择配置文件中的升级脚本完毕: " + upgraders);
		
		//获取最优(执行路径最短)的执行脚本集合
		Set<ExecutableUpgrader> upgradersToExecute = this.getUpgraders(dbVersion, moduleVersion, upgraders);
		
		//整理脚本的执行顺序，并转化成Upgrader类型数组
		return this.getExecScripts(upgradersToExecute, dbVersion, moduleVersion, up.getModuleName());
	}
	
	/**
	 * 获取最优(执行路径最短)的执行脚本集合
	 * @param fromVersion 数据库中的最新版本
	 * @param toVersion 程序模块发布的版本
	 * @param upgraders 经过处理的脚本集合
	 * @return
	 */
	private Set<ExecutableUpgrader> getUpgraders(Version fromVersion, Version toVersion, LinkedHashMap<Version, LinkedHashMap<Version, ExecutableUpgrader>> upgraders) {
		HashSet<ExecutableUpgrader> ups = new HashSet<ExecutableUpgrader>();
		
		if(fromVersion.equals(Version.emptyVersion) && toVersion.equals(Version.emptyVersion)) return new HashSet<ExecutableUpgrader>();
		if(fromVersion.equals(toVersion)) return new HashSet<ExecutableUpgrader>();
		
		if(!upgraders.containsKey(toVersion))
			toVersion = this.findMaxVersion(upgraders);
		
		if(toVersion == null) return new HashSet<ExecutableUpgrader>();
		
		Map<Version, ExecutableUpgrader> map = upgraders.get(toVersion);
		
		//获取最合适的版本(等于或最接近的fromVersion)
		Version newToVersion = this.getVersion(map, fromVersion);
		
		if(newToVersion == null) return new HashSet<ExecutableUpgrader>();
		ups.add(map.get(newToVersion));
		ups.addAll(this.getUpgraders(fromVersion, newToVersion, upgraders));
		
		return ups;
	}

	@SuppressWarnings("unchecked")
	private Version findMaxVersion(LinkedHashMap<Version, LinkedHashMap<Version, ExecutableUpgrader>> upgraders) {
		List<Version> list = new ArrayList<Version>();
		for(Version v : upgraders.keySet()) {
			list.add(v);
		}
		Collections.sort(list);
		if(list.size() == 0) return null;
		else return list.get(list.size()-1);
	}
	/**
	 * 获取最合适的版本(等于或最接近的fromVersion)
	 * @param map
	 * @param fromVersion
	 * @return
	 */
	private Version getVersion(Map<Version, ExecutableUpgrader> map, Version fromVersion) {
		Map<String, Version> sortMap = new HashMap<String, Version>();
		List<String> list = new ArrayList<String>();
		try {
			for(Version v : map.keySet()) {
				int compareValue = this.compare(v, fromVersion);
				sortMap.put(String.valueOf(compareValue), v);
				list.add(String.valueOf(compareValue));
			}
			Collections.sort(list);
			return sortMap.get(list.get(0));
		} catch (Exception e) {
			log.debug(e.getMessage());
			e.printStackTrace();
		} 
		return null;
	}
	
	/*
	 * 暂不用Version的compareTo 
	 * 因为如果 版本(1,2,0)或(1,3,0)与要比较的other(0,0,0)比较时，得到的结果是一样的，都为1；
	 * 再如版本(1,4,2)或(1,4,3)与要比较的other(1,3,3)比较，得到的结果都是1，不符合我们此时的需求。
	 */
	private int compare(Version thiz, Version other) {
		int thizResult= thiz.getMajor() * 10000 + thiz.getMinor() * 100 + thiz.getMicro() * 1;
		int otherResult= other.getMajor() * 10000 + other.getMinor() * 100 + other.getMicro() * 1;
		
		return thizResult - otherResult;
	}
	
	/**
	 * 整理要执行的脚本集合(Set)的先后顺序，核查通过后，转化成Upgrader类型数组。
	 * 只要定义的脚本文件中没有完整的脚本，那么不完全的脚本不执行。
	 * @param upgradersToExecute 筛选后执行最短路径的脚本集合
	 * @param dbVersion 数据库中的版本
	 * @param moduleVersion 程序模块发布的版本
	 * @return
	 */
	private ExecutableUpgrader[] getExecScripts(Set<ExecutableUpgrader> upgradersToExecute, Version dbVersion, Version moduleVersion, String moduleName) {
		int i = 0;
		int len = upgradersToExecute.size();
		ExecutableUpgrader[] actionScripts = new ExecutableUpgrader[len];
		ExecutableUpgrader upgrader = this.getUpgrader(upgradersToExecute, dbVersion);// 找与数据库版本衔接的第一个脚本文件
		
		//不允许出现断层的目的，主要是考虑SQL执行先后的问题，比如在一个x.sql文件中创建表a，另一个y.sql中insert into a
		//必须先执行x.sql才能执行y.sql，目前根据从低版本到高版本的链表先后顺序来执行脚本。如果硬性规定，将类似上述情况，
		//放在一个文件中，可不必考虑断层问题。
		if(upgrader == null) {
			log.warn(moduleName + "版本升级核查：定义的脚本文件不完整，未找到版本"+dbVersion+"！未执行脚本。", 
					new NullPointerException());
			return new ExecutableUpgrader[0];
		}
		
		actionScripts[i] = upgrader;
		for(; i<len-1; i++) {
			Version toVersion = upgrader.getToVersion();
			upgrader = this.getUpgrader(upgradersToExecute, toVersion);
			if(upgrader == null) {
				log.warn(moduleName + "版本升级核查：定义的脚本文件不完整，未找到版本"+toVersion+"！未执行脚本。", 
						new NullPointerException());
				return new ExecutableUpgrader[0];
			}
			actionScripts[i+1] = upgrader;
		}
		return actionScripts;
	}
    
	private ExecutableUpgrader getUpgrader(Set<ExecutableUpgrader> upgradersToExecute, Version fromVersion) {
		ExecutableUpgrader result = null;
		for(ExecutableUpgrader up : upgradersToExecute) {
			if(up.getFromVresion().equals(fromVersion)) {
				result = up;
				break;
			}
		}
		return result;
	}
	
	/**
	 * 执行SQL 
	 * @param sqlList
	 */
	private void execSql(String[] sqlList) {
		this.jdbcTemplate.batchUpdate(sqlList);
	}
	
	/**
	 * 更新数据库中的版本
	 * @param actionScript 定义的升级脚本
	 * @param moduleName 模块名称
	 */
	private void updateVersion(String moduleName, Version releaseVer) {
		String[] sqlList = new String[2];
		sqlList[0] = "delete from BAP_MODULEVERSION where moduleName = '" + moduleName + "'";
		SimpleDateFormat dateformat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = dateformat.format(new Date());
		sqlList[1] = "insert into BAP_MODULEVERSION(ModuleName,Version,Updatetime) values('" + moduleName + "','" + releaseVer.toString() + "','" + date + "')";
		
		this.execSql(sqlList);
	}
	
	/**
	 * 版本格式化
	 * @param version 版本
	 * @return
	 */
	private Version versionFormat(Version version){
		int major = version.getMajor();
		int minor = version.getMinor();
		int micro = version.getMicro();
		
		return new Version(major, minor, micro);
	}

	private Version getModuleVersion(String name) {
		String sql = "select version from BAP_MODULEVERSION where ModuleName = '" + name + "'";
		List<Map<String, Object>> list = this.jdbcTemplate.queryForList(sql);
		if(list.size() == 0)
			return Version.emptyVersion;
		Map<String, Object> map = (Map<String, Object>)list.get(0);
		
		return new Version(map.get("version").toString());
	}
	
	public void setJdbcTemplate(JdbcOperations jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
//		int dataTableCount = jdbcTemplate.queryForInt("select count(*) from user_objects where object_name = 'BAP_MODULEVERSION'");
//		if(dataTableCount == 0) {
//			log.debug("正在创建表BAP_MODULEVERSION....");
//			
//			this.jdbcTemplate.execute("CREATE TABLE BAP_MODULEVERSION" +
//					" (MODULENAME NVARCHAR2(100) NOT NULL ENABLE," +
//					" \"VERSION\" NVARCHAR2(10) NOT NULL ENABLE," +
//					" UPDATETIME DATE NOT NULL ENABLE ," +
//					"  CONSTRAINT \"moduleversion_KEY\" PRIMARY KEY (\"MODULENAME\"))");
//			
//			log.debug("创建表BAP_MODULEVERSION成功");
//		}
	}
}
