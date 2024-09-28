package api.model;

import api.util.Utils;

import java.util.List;
import java.util.Map;

public class APITestCase {
    private String tcid;
    private String name;
    private String descriptions;
    private List<String> conditions;
    private String endpointKey;
    private String headersTemplateKey;
    private List<String> headerOverride;
    private String bodyTemplateKey;
    private List<String> bodyOverride;
    private boolean run;
    private List<String> tags;
    private int expStatus;
    private List<String> expResult;
    private List<String> saveFields;
    private String dynamicValidationTCID;
    private Map<String, String> dynamicValidationExpectedChanges;
    private List<String> queryParams;
    private List<String> pathParams;

    // Getters and setters
    public String getTCID() { return tcid; }
    public void setTCID(String tcid) { this.tcid = tcid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescriptions() { return descriptions; }
    public void setDescriptions(String descriptions) { this.descriptions = descriptions; }

    public List<String> getConditions() { return conditions; }
    public void setConditions(List<String> conditions) { this.conditions = conditions; }

    public String getEndpointKey() { return endpointKey; }
    public void setEndpointKey(String endpointKey) { this.endpointKey = endpointKey; }

    public String getHeadersTemplateKey() { return headersTemplateKey; }
    public void setHeadersTemplateKey(String headersTemplateKey) { this.headersTemplateKey = headersTemplateKey; }

    public List<String> getHeaderOverride() { return headerOverride; }
    public void setHeaderOverride(List<String> headerOverride) { this.headerOverride = headerOverride; }

    public String getBodyTemplateKey() { return bodyTemplateKey; }
    public void setBodyTemplateKey(String bodyTemplateKey) { this.bodyTemplateKey = bodyTemplateKey; }

    public List<String> getBodyOverride() { return bodyOverride; }
    public void setBodyOverride(List<String> bodyOverride) { this.bodyOverride = bodyOverride; }

    public boolean isRun() { return run; }
    public void setRun(boolean run) { this.run = run; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public int getExpStatus() { return expStatus; }
    public void setExpStatus(int expStatus) { this.expStatus = expStatus; }

    public List<String> getExpResult() { return expResult; }
    public void setExpResult(List<String> expResult) { this.expResult = expResult; }
    public Map<String, String> getExpResultAsMap() {
        return Utils.parseKeyValuePairs(expResult);
    }
    public List<String> getSaveFields() { return saveFields; }
    public void setSaveFields(List<String> saveFields) { this.saveFields = saveFields; }

    public String getDynamicValidationTCID() { return dynamicValidationTCID; }
    public void setDynamicValidationTCID(String dynamicValidationTCID) { this.dynamicValidationTCID = dynamicValidationTCID; }

    public Map<String, String> getDynamicValidationExpectedChanges() { return dynamicValidationExpectedChanges; }
    public void setDynamicValidationExpectedChanges(Map<String, String> dynamicValidationExpectedChanges) { this.dynamicValidationExpectedChanges = dynamicValidationExpectedChanges; }

    public List<String> getQueryParams() { return queryParams; }
    public void setQueryParams(List<String> queryParams) { this.queryParams = queryParams; }

    public List<String> getPathParams() { return pathParams; }
    public void setPathParams(List<String> pathParams) { this.pathParams = pathParams; }
    // Validation method
    public boolean isValid() {
        return tcid != null && !tcid.isEmpty() &&
                name != null && !name.isEmpty() &&
                endpointKey != null && !endpointKey.isEmpty() &&
                expStatus > 0;
    }

    @Override
    public String toString() {
        return "APITestCase{" +
                "tcid='" + tcid + '\'' +
                ", name='" + name + '\'' +
                ", endpointKey='" + endpointKey + '\'' +
                ", expStatus=" + expStatus +
                '}';
    }
}