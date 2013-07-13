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


package com.samknows.measurement.schedule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.samknows.measurement.CachingStorage;
import com.samknows.measurement.Constants;
import com.samknows.measurement.TestParamsManager;
import com.samknows.measurement.util.IdGenerator;
import com.samknows.measurement.util.OtherUtils;
import com.samknows.measurement.util.XmlUtils;
import com.samknows.tests.Param;
import com.samknows.tests.TestFactory;


public class TestDescription implements Serializable{
	private static final long serialVersionUID = 1L;
	
	//xml tag definition
	public static final String XML_TYPE = "type";
	public static final String XML_TEST_ID = "test-id";
	public static final String XML_CONDITION_GROUP_ID = "condition-group-id";
	public static final String XML_DISPLAY_NAME = "displayName";
	public static final String XML_IS_PRIMARY = "isPrimary";
	public static final String XML_TIME = "time";
	public static final String XML_PARAMS = "params";
	public static final String XML_PARAM = "param";
	public static final String XML_PARAM_NAME = "name";
	public static final String XML_PARAM_VALUE = "value";
	public static final String XML_POSITION = "position";
	public static final String XML_FIELD = "field";
	public static final String XML_FAILOVER_PARAMS = "failover-params";
	

	public static final long NO_START_TIME = -1;
	
	public long id = IdGenerator.generate();

	public int testId;
	public String type;//todo change to enum
	public boolean isPrimary = false;
	public String displayName;
	public String conditionGroupId;
	public long maxUsageBytes;
	public List<Long> times; //test start times during day
	
	public List<Param> params;
	public List<Param> failoverParams;
	public List<OutParamDescription> outParamsDescription;
	
	public static TestDescription parseXml(Element node) {
		TestDescription td = new TestDescription();
		td.type = node.getAttribute(XML_TYPE);
		String string_test_id = node.getAttribute(XML_TEST_ID);
		if(string_test_id != null && !string_test_id.equals("") ){
			td.testId = Integer.parseInt(string_test_id);
		}
		td.conditionGroupId = node.getAttribute(XML_CONDITION_GROUP_ID);
		td.displayName = OtherUtils.stringEncoding(node.getAttribute(XML_DISPLAY_NAME));
		try {
			td.isPrimary = Boolean.valueOf(node.getAttribute(XML_IS_PRIMARY));
			
		} catch (Exception e) {
			td.isPrimary = false;
		}
		
		td.times = new ArrayList<Long>();
		NodeList list = node.getElementsByTagName(XML_TIME);
		for (int i = 0; i < list.getLength(); i++) {
			Element ep = (Element) list.item(i);
			Long time = XmlUtils.convertTestStartTime(ep.getFirstChild().getNodeValue());
			td.times.add(time);
		}
		Collections.sort(td.times, new Comparator<Long>() {
			@Override
			public int compare(Long lhs, Long rhs) {
				return lhs.compareTo(rhs);
			}
		});
		
		td.params = new ArrayList<Param>();
		NodeList params = node.getElementsByTagName(XML_PARAMS);
		if(params.getLength() == 1){
			NodeList test_params = ((Element)params.item(0)).getElementsByTagName(XML_PARAM);
			for (int i = 0; i < test_params.getLength(); i++) {
				Element ep = (Element) test_params.item(i);
				td.params.add(new Param(ep.getAttribute(XML_PARAM_NAME), ep.getAttribute(XML_PARAM_VALUE)));
			}
			td.maxUsageBytes = TestFactory.getMaxUsage(td.type,td.params);
		}
		
		
		
		NodeList failoverParams = node.getElementsByTagName(XML_FAILOVER_PARAMS);
		td.failoverParams = new ArrayList<Param>();
		if(failoverParams.getLength() == 1){
			NodeList test_params = ((Element)failoverParams.item(0)).getElementsByTagName(XML_PARAMS);
			for(int i=0; i< test_params.getLength(); i++){
				Element ep = (Element) test_params.item(i);
				td.failoverParams.add(new Param(ep.getAttribute(XML_PARAM_NAME), ep.getAttribute(XML_PARAM_VALUE)));
			}
		}
		
		td.outParamsDescription = new ArrayList<OutParamDescription>();
		NodeList outParams = node.getElementsByTagName(XML_FIELD);
		for (int i = 0; i < outParams.getLength(); i++) {
			Element ep = (Element) outParams.item(i);
			OutParamDescription pd = new OutParamDescription();
			pd.name = ep.getAttribute(XML_PARAM_NAME);
			pd.idx = Integer.parseInt(ep.getAttribute(XML_POSITION));
			td.outParamsDescription.add(pd);
		}
		return td;
	}
	
	public boolean canExecute() {
		TestParamsManager manager = CachingStorage.getInstance().loadParamsManager(); 
		boolean isClosestParamSet = manager == null ? false : manager.hasParam("closest");
		return isClosestParamSet || type.equalsIgnoreCase(Constants.TEST_TYPE_CLOSEST_TARGET);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestDescription other = (TestDescription) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
}
