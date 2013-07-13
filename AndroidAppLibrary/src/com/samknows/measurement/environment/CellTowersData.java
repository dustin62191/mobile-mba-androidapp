/*
2013 Measuring Broadband America Program
Mobile Measurement Android Application
Copyright (C) 2012  SamKnows Ltd.

The FCC Measuring Broadband America (MBA) Program's Mobile Measurement Effort developed in cooperation with SamKnows Ltd. and diverse stakeholders employs an client-server based anonymized data collection approach to gather broadband performance data in an open and transparent manner with the highest commitment to protecting participants privacy.  All data collected is thoroughly analyzed and processed prior to public release to ensure that subscribers’ privacy interests are protected.

Data related to the radio characteristics of the handset, information about the handset type and operating system (OS) version, the GPS coordinates available from the handset at the time each test is run, the date and time of the observation, and the results of active test results are recorded on the handset in JSON(JavaScript Object Notation) nested data elements within flat files.  These JSON files are then transmitted to storage servers at periodic intervals after the completion of active test measurements.

This Android application source code is made available under the GNU GPL2 for testing purposes only and intended for participants in the SamKnows/FCC Measuring Broadband American program.  It is not intended for general release and this repository may be disabled at any time.


This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/


package com.samknows.measurement.environment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Build;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.SignalStrength;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

import com.samknows.measurement.Logger;
import com.samknows.measurement.storage.PassiveMetric;
import com.samknows.measurement.util.DCSConvertorUtil;
import com.samknows.measurement.util.DCSStringBuilder;

public class CellTowersData implements DCSData{
	private static final String ID = "CELLTOWER";
	private static final String ID_NEIGHBOR = "CELLTOWERNEIGHBOR";
	private static final String GSM = "GSM";
	private static final String CDMA = "CDMA";
	private static final String VERSION = ".2";
	

	//JSONOutput for the neighbour cell tower
	public static final String JSON_TYPE_CELL_TOWER_NEIGHBOUR= "cell_neighbour_tower_data";
	public static final String JSON_CELL_TOWER_ID= "cell_tower_id";
	public static final String JSON_LOCATION_AREA_CODE = "location_area_code";
	public static final String JSON_UMTS_PSC = "umts_psc";
	public static final String JSON_RSSI = "rssi";
	public static final String JSON_NETWORK_TYPE = "network_type";
	public static final String JSON_NETWORK_TYPE_CODE = "network_type_code";
	
	public static final String JSON_TYPE_GSM_CELL_LOCATION = "gsm_cell_location";
	public static final String JSON_TYPE_CDMA_CELL_LOCATION = "cdma_cell_location";
	public static final String JSON_BASE_STATION_ID = "base_station_id";
	public static final String JSON_BASE_STATION_LATITUDE = "base_station_latitude";
	public static final String JSON_BASE_STATION_LONGITUDE = "base_station_longitude";
	public static final String JSON_NETWORK_ID = "netwok_id";
	public static final String JSON_SYSTEM_ID = "system_id";
	public static final String JSON_SIGNAL_STRENGTH = "signal_strength";
	public static final String JSON_BIT_ERROR_RATE = "bit_error_rate";
	public static final String JSON_DBM = "dbm";
	public static final String JSON_ECIO = "ecio";
	
	
	
	
	public CellLocation cellLocation;
	public SignalStrength signal;
	/** time in milis */
	public long time;
	public List<NeighboringCellInfo> neighbors;
	public int network_type;
	@Override
	public List<String> convert() {
		List<String> list = new ArrayList<String>();
		addCellData(list);
		for (NeighboringCellInfo cellInfo : neighbors) {
			addCellData(list, cellInfo);
		}
		return list;
	}
	
	
	private void addCellData(List<String> list, NeighboringCellInfo cellInfo) {
		DCSStringBuilder builder = new DCSStringBuilder();
		builder.append(ID_NEIGHBOR);
		builder.append(time/1000);
		builder.append(cellInfo.getCid());
		builder.append(cellInfo.getLac());
		builder.append(cellInfo.getPsc() );
		builder.append(cellInfo.getRssi());
		builder.append(DCSConvertorUtil.convertNetworkType(cellInfo.getNetworkType()));
		list.add(builder.build());
	}

	private void addCellData(List<String> list) {
		DCSStringBuilder builder = new DCSStringBuilder();
		
		if (cellLocation instanceof GsmCellLocation) {
			GsmCellLocation gsmLocation = (GsmCellLocation) cellLocation;
			builder.append(ID + GSM + VERSION);
			builder.append(time/1000);
			builder.append(GSM);
			builder.append(gsmLocation.getCid());
			builder.append(gsmLocation.getLac());
			builder.append(Build.VERSION.SDK_INT >= 9 ? gsmLocation.getPsc() : -1 );
			
		} else if (cellLocation instanceof CdmaCellLocation) {
			CdmaCellLocation cdmaLocation = (CdmaCellLocation) cellLocation;
			builder.append(ID + CDMA);
			builder.append(time/1000);
			builder.append(CDMA);
			builder.append(cdmaLocation.getBaseStationId());
			builder.append(cdmaLocation.getBaseStationLatitude());
			builder.append(cdmaLocation.getBaseStationLongitude());
			builder.append(cdmaLocation.getNetworkId());
			builder.append(cdmaLocation.getSystemId());
		}
		
		if (signal.isGsm()) {
			builder.append(signal.getGsmSignalStrength());
			builder.append(signal.getGsmBitErrorRate());
		} else {
			builder.append(signal.getCdmaDbm());
			builder.append(signal.getCdmaEcio());
		}
		list.add(builder.build());
	}

	
	
	
	@Override
	public List<JSONObject> getPassiveMetric() {
		List<JSONObject> ret = new ArrayList<JSONObject>();
		if(cellLocation instanceof GsmCellLocation){
			ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.GSMLAC,time,((GsmCellLocation) cellLocation).getLac()+""));
			ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.GSMCID,time,((GsmCellLocation) cellLocation).getCid()+""));
		}else if(cellLocation instanceof CdmaCellLocation){
			CdmaCellLocation cdmaLocation = (CdmaCellLocation) cellLocation;
			ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.CDMABSID, time, cdmaLocation.getBaseStationId()+""));
			if (cdmaLocation.getBaseStationLatitude() != Integer.MAX_VALUE) {
				ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.CDMABSLAT, time,cdmaLocation.getBaseStationLatitude()+""));
			} 
			if (cdmaLocation.getBaseStationLongitude() != Integer.MAX_VALUE) {
				ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.CDMABSLNG, time, cdmaLocation.getBaseStationLongitude()+""));
			}
			ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.CDMANETWORKID,time,cdmaLocation.getNetworkId()+""));
			ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.CDMASYSTEMID,time, cdmaLocation.getSystemId()+""));
		}
		if (signal.isGsm()) {
			ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.GSMSIGNALSTRENGTH,time, DCSConvertorUtil.convertGsmSignalStrength(signal.getGsmSignalStrength())));
		//	ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.GSMBER, time, DCSConvertorUtil.convertGsmBitErroRate(signal.getGsmBitErrorRate())));
		} else {
			ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.CDMADBM,time, signal.getCdmaDbm()+""));
			ret.add(PassiveMetric.create(PassiveMetric.METRIC_TYPE.CDMAECIO,time, signal.getCdmaEcio()+""));
		}
		return ret;
	}


	@Override
	public List<JSONObject> convertToJSON() {
		List<JSONObject> ret = new ArrayList<JSONObject>();
		
		try{
			JSONObject cellLoc = null;
			if(cellLocation instanceof GsmCellLocation){
				GsmCellLocation l = (GsmCellLocation) cellLocation;
				JSONObject gsm = new JSONObject();
				gsm.put(JSON_TYPE, JSON_TYPE_GSM_CELL_LOCATION);
				gsm.put(JSON_TIMESTAMP, time/1000+"");
				gsm.put(JSON_DATETIME, new java.util.Date(time).toString());
				gsm.put(JSON_CELL_TOWER_ID, l.getCid()+"");
				gsm.put(JSON_LOCATION_AREA_CODE, l.getLac());
				gsm.put(JSON_UMTS_PSC, Build.VERSION.SDK_INT >= 9 ? l.getPsc()+"" : "-1");
				if(signal.isGsm()){
					gsm.put(JSON_SIGNAL_STRENGTH, signal.getGsmSignalStrength()+"");
					//gsm.put(JSON_BIT_ERROR_RATE, signal.getGsmBitErrorRate()+"");
				}
				ret.add(gsm);
				
			}else if(cellLocation instanceof CdmaCellLocation){
				CdmaCellLocation l = (CdmaCellLocation) cellLocation;
				JSONObject cdma = new JSONObject();
				cdma.put(JSON_TYPE,JSON_TYPE_CDMA_CELL_LOCATION);
				cdma.put(JSON_TIMESTAMP, time/1000+"");
				cdma.put(JSON_DATETIME, new java.util.Date(time).toString());
				cdma.put(JSON_BASE_STATION_ID, l.getBaseStationId()+"");
				cdma.put(JSON_BASE_STATION_LATITUDE, l.getBaseStationLatitude()+"");
				cdma.put(JSON_BASE_STATION_LONGITUDE, l.getBaseStationLongitude()+"");
				cdma.put(JSON_SYSTEM_ID, l.getSystemId()+"");
				cdma.put(JSON_NETWORK_ID, l.getNetworkId()+"");
				if(!signal.isGsm()){
					cdma.put(JSON_DBM, signal.getCdmaDbm()+"");
					cdma.put(JSON_ECIO, signal.getCdmaEcio()+"");
				}
				ret.add(cdma);
			}
			
			for (NeighboringCellInfo cellInfo : neighbors) {
				JSONObject neighbour = new JSONObject();
				neighbour.put(JSON_TYPE, JSON_TYPE_CELL_TOWER_NEIGHBOUR);
				neighbour.put(JSON_TIMESTAMP, time/1000+"");
				neighbour.put(JSON_DATETIME, new java.util.Date(time).toString());
				neighbour.put(JSON_NETWORK_TYPE_CODE,cellInfo.getNetworkType()+"");
				neighbour.put(JSON_NETWORK_TYPE, DCSConvertorUtil.convertNetworkType(cellInfo.getNetworkType()));
				neighbour.put(JSON_RSSI, cellInfo.getRssi()+"");
				neighbour.put(JSON_UMTS_PSC, cellInfo.getPsc()+"");
				neighbour.put(JSON_CELL_TOWER_ID, cellInfo.getCid()+"");
				neighbour.put(JSON_LOCATION_AREA_CODE, cellInfo.getLac()+"");
				ret.add(neighbour);
			}
			
			
		}catch(JSONException je){
			Logger.e(CellTowersData.class, "Error in creating JSONObject: "+ je.getMessage());
			ret = null;
		}
		return ret;
	}
}
