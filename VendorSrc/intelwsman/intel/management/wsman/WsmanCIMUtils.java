package intel.management.wsman;

import java.util.HashMap;

import org.sblim.wbem.cim.CIMDataType;
import org.sblim.wbem.cim.CIMElement;
import org.sblim.wbem.cim.CIMInstance;
import org.sblim.wbem.cim.CIMProperty;
import org.sblim.wbem.cim.CIMValue;
import org.sblim.wbem.cim.UnsignedInt64;
import org.sblim.wbem.cimxml.CIMXmlUtil;
import org.sblim.wbem.cimxml.CIMXmlUtilFactory;

public class WsmanCIMUtils {
   
    public static HashMap<String, String> getKvpExchangeData(ManagedInstance mi) throws WsmanException{
        HashMap<String, String> retval = new HashMap<String, String>();
        try {
            CIMXmlUtil cimXmlUtil = CIMXmlUtilFactory.getCIMXmlUtil();
            for(Object obj : mi.getPropertyArray("GuestIntrinsicExchangeItems")){
                CIMElement element = cimXmlUtil.getCIMElement(obj.toString());
                CIMInstance instance = (CIMInstance)element;
                retval.put(instance.getProperty("Name").getValue().getValue().toString(), instance.getProperty("Data").getValue().getValue().toString());
            }
       }
       catch (Exception exception) {
            throw new WsmanException(exception);
       }
       return retval;
    }
    
    public static String createMsvmMemorySettingData(ManagedInstance memorySettingData) throws WsmanException{
        String cimInstanceString = null;
        try {
            CIMXmlUtil cimXmlUtil = CIMXmlUtilFactory.getCIMXmlUtil();
            CIMElement element = cimXmlUtil.getCIMElement("<INSTANCE CLASSNAME=\"Msvm_MemorySettingData\"/>");
            CIMInstance instance = (CIMInstance)element;
            
            addCIMProperty(instance, memorySettingData, "Address", CIMDataType.STRING, true, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, memorySettingData, "AllocationUnits", CIMDataType.STRING, false, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, memorySettingData, "AutomaticAllocation", CIMDataType.BOOLEAN, false, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, memorySettingData, "AutomaticDeallocation", CIMDataType.BOOLEAN, false, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, memorySettingData, "Caption", CIMDataType.STRING, false, "CIM_ManagedElement");
            addCIMProperty(instance, memorySettingData, "Connection", CIMDataType.STRING_ARRAY, true, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, memorySettingData, "ConsumerVisibility", CIMDataType.UINT16, true, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, memorySettingData, "Description", CIMDataType.STRING, false, "CIM_ManagedElement");
            addCIMProperty(instance, memorySettingData, "DeviceID", CIMDataType.STRING, true, "Msvm_MemorySettingData");
            addCIMProperty(instance, memorySettingData, "DeviceIDFormat", CIMDataType.STRING, true, "Msvm_MemorySettingData");
            addCIMProperty(instance, memorySettingData, "DynamicMemoryEnabled", CIMDataType.BOOLEAN, true, "Msvm_MemorySettingData");
            addCIMProperty(instance, memorySettingData, "ElementName", CIMDataType.STRING, false, "CIM_ManagedElement");
            addCIMProperty(instance, memorySettingData, "HostResource", CIMDataType.STRING_ARRAY, true, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, memorySettingData, "InstanceID", CIMDataType.STRING, false, "CIM_SettingData");
            addCIMProperty(instance, memorySettingData, "IsVirtualized", CIMDataType.BOOLEAN, false, "Msvm_MemorySettingData");
            addCIMProperty(instance, memorySettingData, "Limit", CIMDataType.UINT64, true, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, memorySettingData, "MappingBehavior", CIMDataType.UINT16, true, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, memorySettingData, "OtherResourceType", CIMDataType.STRING, false, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, memorySettingData, "Parent", CIMDataType.STRING, true, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, memorySettingData, "PoolID", CIMDataType.STRING, false, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, memorySettingData, "Reservation", CIMDataType.UINT64, false, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, memorySettingData, "ResourceSubType", CIMDataType.STRING, false, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, memorySettingData, "ResourceType", CIMDataType.UINT16, false, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, memorySettingData, "VirtualQuantity", CIMDataType.UINT64, false, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, memorySettingData, "Weight", CIMDataType.UINT32, false, "CIM_ResourceAllocationSettingData");
            
            cimInstanceString = cimXmlUtil.CIMElementToXml(instance);
            
            cimInstanceString = cimInstanceString.substring(cimInstanceString.indexOf("<", 2));
        } catch (Exception e) {
            throw new WsmanException(e);
        }
        return cimInstanceString;
    }
    
    public static String createMsvmProcessorSettingData(ManagedInstance processorSettingData) throws WsmanException{
        String cimInstanceString = null;
        try {
            CIMXmlUtil cimXmlUtil = CIMXmlUtilFactory.getCIMXmlUtil();
            CIMElement element = cimXmlUtil.getCIMElement("<INSTANCE CLASSNAME=\"Msvm_ProcessorSettingData\"/>");
            CIMInstance instance = (CIMInstance)element;
            
            addCIMProperty(instance, processorSettingData, "Address", CIMDataType.STRING, true, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, processorSettingData, "AllocationUnits", CIMDataType.STRING, false, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, processorSettingData, "AutomaticAllocation", CIMDataType.BOOLEAN, false, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, processorSettingData, "AutomaticDeallocation", CIMDataType.BOOLEAN, false, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, processorSettingData, "Caption", CIMDataType.STRING, false, "CIM_ManagedElement");
            addCIMProperty(instance, processorSettingData, "Connection", CIMDataType.STRING_ARRAY, true, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, processorSettingData, "ConsumerVisibility", CIMDataType.UINT16, true, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, processorSettingData, "Description", CIMDataType.STRING, false, "CIM_ManagedElement");
            addCIMProperty(instance, processorSettingData, "DeviceID", CIMDataType.STRING, true, "Msvm_ProcessorSettingData");
            addCIMProperty(instance, processorSettingData, "DeviceIDFormat", CIMDataType.STRING, true, "Msvm_ProcessorSettingData");
            addCIMProperty(instance, processorSettingData, "ElementName", CIMDataType.STRING, false, "CIM_ManagedElement");
            addCIMProperty(instance, processorSettingData, "HostResource", CIMDataType.STRING_ARRAY, true, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, processorSettingData, "InstanceID", CIMDataType.STRING, false, "CIM_SettingData");
            addCIMProperty(instance, processorSettingData, "IsVirtualized", CIMDataType.BOOLEAN, false, "Msvm_ProcessorSettingData");
            addCIMProperty(instance, processorSettingData, "Limit", CIMDataType.UINT64, false, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, processorSettingData, "LimitCPUID", CIMDataType.BOOLEAN, true, "Msvm_ProcessorSettingData");
            addCIMProperty(instance, processorSettingData, "LimitProcessorFeatures", CIMDataType.BOOLEAN, true, "Msvm_ProcessorSettingData");
            addCIMProperty(instance, processorSettingData, "MappingBehavior", CIMDataType.UINT16, true, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, processorSettingData, "OtherResourceType", CIMDataType.STRING, false, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, processorSettingData, "Parent", CIMDataType.STRING, true, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, processorSettingData, "PoolID", CIMDataType.STRING, false, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, processorSettingData, "ProcessorsPerSocket", CIMDataType.UINT16, false, "Msvm_ProcessorSettingData");
            addCIMProperty(instance, processorSettingData, "Reservation", CIMDataType.UINT64, false, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, processorSettingData, "ResourceSubType", CIMDataType.STRING, false, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, processorSettingData, "ResourceType", CIMDataType.UINT16, false, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, processorSettingData, "SocketCount", CIMDataType.UINT16, false, "Msvm_ProcessorSettingData");
            addCIMProperty(instance, processorSettingData, "ThreadsEnabled", CIMDataType.BOOLEAN, false, "Msvm_ProcessorSettingData");
            addCIMProperty(instance, processorSettingData, "VirtualQuantity", CIMDataType.UINT64, false, "CIM_ResourceAllocationSettingData");
            addCIMProperty(instance, processorSettingData, "Weight", CIMDataType.UINT32, false, "CIM_ResourceAllocationSettingData");
            
            cimInstanceString = cimXmlUtil.CIMElementToXml(instance);
            
            cimInstanceString = cimInstanceString.substring(cimInstanceString.indexOf("<", 2));
        } catch (Exception e) {
            throw new WsmanException(e);
        }
        return cimInstanceString;
    }
    
    public static String createMsvmVirtualSystemExportSettingData(ManagedInstance exportSettingData) throws WsmanException{
        String cimInstanceString = null;
        try {
            CIMXmlUtil cimXmlUtil = CIMXmlUtilFactory.getCIMXmlUtil();
            CIMElement element = cimXmlUtil.getCIMElement("<INSTANCE CLASSNAME=\"Msvm_VirtualSystemExportSettingData\"/>");
            CIMInstance instance = (CIMInstance)element;
            
            addCIMProperty(instance, exportSettingData, "Caption", CIMDataType.STRING, false, "CIM_SettingData");
            addCIMProperty(instance, exportSettingData, "CopySnapshotConfiguration", CIMDataType.UINT8, true, "Msvm_VirtualSystemExportSettingData");
            addCIMProperty(instance, exportSettingData, "CopyVmRuntimeInformation", CIMDataType.BOOLEAN, true, "Msvm_VirtualSystemExportSettingData");
            addCIMProperty(instance, exportSettingData, "CopyVmStorage", CIMDataType.BOOLEAN, true, "Msvm_VirtualSystemExportSettingData");
            addCIMProperty(instance, exportSettingData, "CreateVmExportSubdirectory", CIMDataType.BOOLEAN, true, "Msvm_VirtualSystemExportSettingData");
            addCIMProperty(instance, exportSettingData, "Description", CIMDataType.STRING, false, "CIM_SettingData");
            addCIMProperty(instance, exportSettingData, "ElementName", CIMDataType.STRING, false, "CIM_SettingData");
            addCIMProperty(instance, exportSettingData, "InstanceID", CIMDataType.STRING, false, "CIM_SettingData");
            
            cimInstanceString = cimXmlUtil.CIMElementToXml(instance);
            
            cimInstanceString = cimInstanceString.substring(cimInstanceString.indexOf("<", 2));
        } catch (Exception e) {
            throw new WsmanException(e);
        }
        return cimInstanceString;
    }
    
    public static String createMsvmVirtualSystemImportSettingData(ManagedInstance importSettingData) throws WsmanException{
        String cimInstanceString = null;
        try {
            CIMXmlUtil cimXmlUtil = CIMXmlUtilFactory.getCIMXmlUtil();
            CIMElement element = cimXmlUtil.getCIMElement("<INSTANCE CLASSNAME=\"Msvm_VirtualSystemImportSettingData\"/>");
            CIMInstance instance = (CIMInstance)element;
            
            addCIMProperty(instance, importSettingData, "Caption", CIMDataType.STRING, false, "CIM_SettingData");
            addCIMProperty(instance, importSettingData, "CreateCopy", CIMDataType.BOOLEAN, true, "Msvm_VirtualSystemImportSettingData");
            addCIMProperty(instance, importSettingData, "CurrentResourcePaths", CIMDataType.STRING_ARRAY, true, "Msvm_VirtualSystemImportSettingData");
            addCIMProperty(instance, importSettingData, "Description", CIMDataType.STRING, false, "CIM_SettingData");
            addCIMProperty(instance, importSettingData, "ElementName", CIMDataType.STRING, false, "CIM_SettingData");
            addCIMProperty(instance, importSettingData, "GenerateNewId", CIMDataType.BOOLEAN, true, "Msvm_VirtualSystemImportSettingData");
            addCIMProperty(instance, importSettingData, "InstanceID", CIMDataType.STRING, false, "CIM_SettingData");
            addCIMProperty(instance, importSettingData, "Name", CIMDataType.STRING, false, "Msvm_VirtualSystemImportSettingData");
            addCIMProperty(instance, importSettingData, "SecurityScope", CIMDataType.STRING, false, "Msvm_VirtualSystemImportSettingData");
            addCIMProperty(instance, importSettingData, "SourceNetworkConnections", CIMDataType.STRING_ARRAY, true, "Msvm_VirtualSystemImportSettingData");
            addCIMProperty(instance, importSettingData, "SourceResourcePaths", CIMDataType.STRING_ARRAY, true, "Msvm_VirtualSystemImportSettingData");
            addCIMProperty(instance, importSettingData, "SourceSnapshotDataRoot", CIMDataType.STRING, false, "Msvm_VirtualSystemImportSettingData");
            addCIMProperty(instance, importSettingData, "SourceVhdDataRoot", CIMDataType.STRING, false, "Msvm_VirtualSystemImportSettingData");
            addCIMProperty(instance, importSettingData, "SourceVmDataRoot", CIMDataType.STRING, false, "Msvm_VirtualSystemImportSettingData");
            addCIMProperty(instance, importSettingData, "TargetNetworkConnections", CIMDataType.STRING_ARRAY, true, "Msvm_VirtualSystemImportSettingData");
            addCIMProperty(instance, importSettingData, "TargetResourcePaths", CIMDataType.STRING_ARRAY, true, "Msvm_VirtualSystemImportSettingData");
            addCIMProperty(instance, importSettingData, "TargetSnapshotDataRoot", CIMDataType.STRING, false, "Msvm_VirtualSystemImportSettingData");
            addCIMProperty(instance, importSettingData, "TargetVhdDataRoot", CIMDataType.STRING, false, "Msvm_VirtualSystemImportSettingData");
            addCIMProperty(instance, importSettingData, "TargetVmDataRoot", CIMDataType.STRING, false, "Msvm_VirtualSystemImportSettingData");
            
            cimInstanceString = cimXmlUtil.CIMElementToXml(instance);
            
            cimInstanceString = cimInstanceString.substring(cimInstanceString.indexOf("<", 2));
        } catch (Exception e) {
            throw new WsmanException(e);
        }
        return cimInstanceString;
    }
    
    private static void addCIMProperty(CIMInstance instance, ManagedInstance mi, String propertyName, int dataType, boolean propagated, String originClass){
        if(mi.getProperty(propertyName) == null){
            return;
        }
        CIMProperty property = null;
        try{
            if(dataType == CIMDataType.STRING_ARRAY){
                if(mi.getProperty(propertyName) instanceof String){
                    property = new CIMProperty(propertyName, new CIMValue(new String[]{mi.getProperty(propertyName).toString()}, new CIMDataType(dataType)));
                }
                else if(mi.getProperty(propertyName) instanceof String[]){
                    property = new CIMProperty(propertyName, new CIMValue((String[])mi.getProperty(propertyName), new CIMDataType(dataType)));
                }
            }
            else if(dataType == CIMDataType.STRING){
                property = new CIMProperty(propertyName, new CIMValue(mi.getProperty(propertyName).toString(), new CIMDataType(dataType)));
            }
            else if(dataType == CIMDataType.BOOLEAN){
                property = new CIMProperty(propertyName, new CIMValue(Boolean.parseBoolean(mi.getProperty(propertyName).toString()), new CIMDataType(dataType)));
            }
            else if(dataType == CIMDataType.UINT8 || dataType == CIMDataType.UINT16 || dataType == CIMDataType.UINT32 || dataType == CIMDataType.UINT64){
                property = new CIMProperty(propertyName, new CIMValue(new UnsignedInt64(Integer.parseInt(mi.getProperty(propertyName).toString())), new CIMDataType(dataType)));
            }
            if(property != null){
                property.setOriginClass(originClass);
                if(propagated){
                    property.setPropagated(propagated);
                }
                instance.addProperty(property);
            }
        } catch(NumberFormatException e){
        }
    }
}
