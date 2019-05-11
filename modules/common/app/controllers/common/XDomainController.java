package controllers.common;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import inceptors.common.HeaderAccessAction;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.With;
import play.mvc.Controller;

@With(HeaderAccessAction.class)
public class XDomainController extends Controller {
    public Result success(String name, Object obj) {
        ObjectNode node = Json.newObject();
        node.put("code", 0);

        if (name != null && obj != null) {
            node.set(name, Json.toJson(obj));
        }
        return ok(node);
    }
    
    public Result success() {
        ObjectNode node = Json.newObject();
        node.put("code", 0);

        return ok(node);
    }

    public Result failure(int code) {
        ObjectNode node = Json.newObject();
        node.put("code", code);
        return ok(node);
    }

    public static Result staticSuccess(String name, Object obj) {
        ObjectNode node = Json.newObject();
        node.put("code", 0);

        if (name != null && obj != null) {
            node.set(name, Json.toJson(obj));
        }

        return ok(node);
    }
    
    public static Result staticSuccessString(String name, String value) {
        ObjectNode node = Json.newObject();
        node.put("code", 0);

        if (name != null && value != null) {
            node.put(name, value);
        }

        return ok(node);        
    }
    
    public static Result staticFailure(int code) {
        ObjectNode node = Json.newObject();     
        node.put("code", code);     
        return ok(node);
        
    }    
    
    public static Result successList(int total, int page, List list) {
        ObjectNode node = Json.newObject();
        node.put("totalPage", page);
        node.put("totalNumber", total);
        node.set("list", Json.toJson(list));
        
        return staticSuccess("data", node);
    }
    
    public static Result successListObj(int total, int page, Object obj) {
        ObjectNode node = Json.newObject();
        node.put("totalPage", page);
        node.put("totalNumber", total);
        node.set("obj", Json.toJson(obj));
        
        return staticSuccess("data", node);    	
    }
    
    public static Result successList(int total, int page, JsonNode list) {
        ObjectNode node = Json.newObject();
        node.put("totalPage", page);
        node.put("totalNumber", total);
        node.set("list", list);
        
        return staticSuccess("data", node);
    	
    }

    public static Result successList(List list) {
    	return BaseController.staticSuccess("list", Json.toJson(list));
    }
}
