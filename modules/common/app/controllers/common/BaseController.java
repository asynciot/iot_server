package controllers.common;

import inceptors.common.HeaderAccessAction;
import inceptors.common.Secured;

import play.mvc.Controller;
import play.mvc.With;
import play.mvc.Result;

import java.util.List;

import javax.inject.Inject;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.Logger;
import play.libs.Json;
import play.mvc.Security;
import java.lang.Class;
import com.avaje.ebean.Model;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;

import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.Date;

@Security.Authenticated(Secured.class)
public class BaseController extends XDomainController {
	@Inject
	protected FormFactory formFactory;
	    
	public <T> Result create(Class<T> modelClass) {
		try {
			if (!Model.class.isAssignableFrom(modelClass)) {
				throw new CodeException(ErrDefinition.E_CLASS_NOT_SUPPORT);
			}
			
			Form<T> form = formFactory.form(modelClass).bindFromRequest();
			
			if (form.hasErrors()) {
				throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
			}
			
			T obj = form.get();
			Field idField = modelClass.getDeclaredField("id");
			if (idField.getType().isAssignableFrom(String.class)) {
				idField.setAccessible(true);
        		idField.set(obj, CodeGenerator.generateShortUUId());				
			}			
			
			try {
				Field createTimeField = modelClass.getDeclaredField("createTime");
				if (createTimeField != null) {
					createTimeField.setAccessible(true);
					createTimeField.set(obj, new Date());
				}
			}
			catch (Exception ex1){
				
			}
			
			Ebean.save(obj);
			
			return success("id", idField.get(obj).toString());
		}
		catch (CodeException ce) {
			Logger.error(ce.getMessage());
			return failure(ce.getCode());
		}
		catch (Throwable e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			return failure(ErrDefinition.E_COMMON_CREATE_FAILED);
		}				        
	}
	
	public <T> Result read(Class<T> modelClass) {
		try {
			if (!Model.class.isAssignableFrom(modelClass)) {
				throw new CodeException(ErrDefinition.E_CLASS_NOT_SUPPORT);
			}
			
			List<T> list = null;
			DynamicForm form = formFactory.form().bindFromRequest();
			
			String id = form.get("id");			
			if (id != null && !id.isEmpty()) {
				T obj = null;
				Field idField = modelClass.getDeclaredField("id");
				if (idField.getType().isAssignableFrom(Integer.class)) {
					obj = Ebean.find(modelClass, Integer.parseInt(id));
				}
				else {
					obj = Ebean.find(modelClass, id);
				}
				
				if (obj == null) {
					throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
				}
				
				list = new ArrayList<T>();
				list.add(obj);
				addMoreInfo(list);
				return successList(1, 1, list);
			}
			
            ExpressionList<T> exprList = Ebean.find(modelClass).where();
            String pageStr = form.get("page");
            String numStr = form.get("num");
            if (null == pageStr || pageStr.isEmpty()) {
            	throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
           
            if (null == numStr || numStr.isEmpty()) {
            	throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
           
            Integer page = Integer.parseInt(pageStr);
            Integer num = Integer.parseInt(numStr);
           
            addExprFilter(exprList);
            com.avaje.ebean.Query<T> query = exprList
            		.setFirstRow((page-1)*num)
            		.setMaxRows(num);
            
            Field createTimeField = null;
            try {
            	createTimeField = modelClass.getDeclaredField("createTime");
            }
            catch (Throwable e1) {
            	createTimeField = null;
            }
            
            if (null != createTimeField) {
            	String order = form.get("order");
            	if (order != null) {
                	list = query.orderBy("createTime " + order).findList();            		
            	}
            	else {
                	list = query.orderBy("createTime desc").findList();            		
            	}
            }
            else {
                list = query.findList();            	
            }
                   
            addMoreInfo(list);
            int totalNum = exprList.findRowCount();            
            int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;
                   
            return successList(totalNum, totalPage, list);			
		}
		catch (CodeException ce) {
			Logger.error(ce.getMessage());
			return failure(ce.getCode());
		}
		catch (Throwable e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			return failure(ErrDefinition.E_COMMON_READ_FAILED);
		}				        
	}
	
	protected <T> void addExprFilter(ExpressionList<T> exprList) {
		
	}
	
	protected <T> void addMoreInfo(List<T> list) {
		
	}
	
	
	
	public <T> Result update(Class<T> modelClass) {
		try {
			if (!Model.class.isAssignableFrom(modelClass)) {
				throw new CodeException(ErrDefinition.E_CLASS_NOT_SUPPORT);
			}
			
            Form<T> form = formFactory.form(modelClass).bindFromRequest();
            
            if (form.hasErrors()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            
            T tmp = form.get();
            
			Field idField = modelClass.getDeclaredField("id");
			T obj = Ebean.find(modelClass, idField.get(tmp));
            			
			Logger.info(idField.get(tmp).toString());
            if (obj == null) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);                
            }
			
            Field[] fields = modelClass.getDeclaredFields();
            
            for (Field field : fields) {
            	if (field.getName().compareTo("id") == 0) {
            		continue;
            	}
                field.setAccessible(true);
            	if (field.get(tmp) != null) {
            		field.set(obj, field.get(tmp));
            	}
            }
            
            Ebean.update(obj);
            
            return success();
		}
		catch (CodeException ce) {
			Logger.error(ce.getMessage());
			return failure(ce.getCode());
		}
		catch (Throwable e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			return failure(ErrDefinition.E_COMMON_UPDATE_FAILED);
		}				        
	}
	
	public <T> Result delete(Class<T> modelClass) {
		try {
			if (!Model.class.isAssignableFrom(modelClass)) {
				throw new CodeException(ErrDefinition.E_CLASS_NOT_SUPPORT);
			}
			
			DynamicForm form = formFactory.form().bindFromRequest();
			
			String id = form.get("id");
			Field idField = modelClass.getDeclaredField("id");
			T obj = Ebean.find(modelClass, id);
			
			if (obj == null) {
				throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
			}
			
			Ebean.delete(obj);
			return success();
		}
		catch (CodeException ce) {
			Logger.error(ce.getMessage());
			return failure(ce.getCode());
		}
		catch (Throwable e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			return failure(ErrDefinition.E_COMMON_DELETE_FAILED);
		}				        
	}
}
