package com.csnt.ins.model.yg;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;

/**
 * Generated by JFinal, do not modify this file.
 * <pre>
 * Example:
 * public void configPlugin(Plugins me) {
 *     ActiveRecordPlugin arp = new ActiveRecordPlugin(...);
 *     _MappingKit.mapping(arp);
 *     me.add(arp);
 * }
 * </pre>
 */
public class _MappingKit {
	
	public static void mapping(ActiveRecordPlugin arp) {
		// Composite Primary Key order: CARDID,ID
		arp.addMapping("TBL_OBU", "CARDID,ID", TblObu.class);
		arp.addMapping("TBL_USERCARD", "ID", TblUsercard.class);
		arp.addMapping("TBL_VEHICLE", "ID", TblVehicle.class);
	}
}

