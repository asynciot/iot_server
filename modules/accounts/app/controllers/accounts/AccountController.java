package controllers.accounts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.common.XDomainController;
import controllers.common.CodeException;
import controllers.common.CodeGenerator;
import controllers.common.ErrDefinition;
import inceptors.common.HeaderAccessAction;
import inceptors.common.Secured;
import models.accounts.Account;
import models.accounts.Authority;
//import models.accounts.AccountCert;
//import models.accounts.AccountLabel;
//import models.accounts.Certification;
//import models.accounts.Label;
//import models.utils.SmsRecord;
import play.Logger;
import play.cache.CacheApi;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import java.util.Map.Entry;


public class AccountController extends XDomainController {

	@Inject
	private FormFactory formFactory;
	
	@Inject
	private CacheApi cache;
	
	@Security.Authenticated(Secured.class)
	public Result create() {
		Form<Account> form = formFactory.form(Account.class);
				
		try {
			String userId = session("userId");
			Account adminAccount = Account.find.byId(userId);
			
			if (adminAccount == null) {
				throw new CodeException(ErrDefinition.E_ACCOUNT_INCORRECT_PARAM);				
			}
			
			if (adminAccount.roleType != Authority.E_ADMIN &&
			    adminAccount.roleType != Authority.E_SUPERADMIN) {
				throw new CodeException(ErrDefinition.E_ACCOUNT_UNAUTHENTICATED);				
			}
						
			if (form.hasErrors()) {		
				throw new CodeException(ErrDefinition.E_ACCOUNT_INCORRECT_PARAM);
			}
 
			Account newAccount = form.bindFromRequest().get();
			if (Account.find.where().eq("username", newAccount.username).findRowCount() != 0) {
				throw new CodeException(ErrDefinition.E_ACCOUNT_ALREADY_EXIST);
			}
			
            if (newAccount.roleType == 0) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_INCORRECT_PARAM);               
            }

            if (newAccount.password == null || newAccount.password.isEmpty()) {
				throw new CodeException(ErrDefinition.E_ACCOUNT_NO_PASSWORD);
			}
			
			if (newAccount.password != null && !newAccount.password.isEmpty()) {
				newAccount.password = CodeGenerator.generateMD5(newAccount.password);				
			}
			
			newAccount.id = CodeGenerator.generateShortUUId();
			newAccount.createTime = new Date();
//			newAccount.type = 0;

//			newAccount.roleType = 0;
//			newAccount.isEnabled = true;
//			newAccount.mobile = "";
			
			Ebean.save(newAccount);
			
			return success();
		}
		catch (CodeException ce) {
			Logger.error(ce.getMessage());
			return failure(ce.getCode());
		}
		catch (Throwable e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			return failure(ErrDefinition.E_ACCOUNT_CREATE_FAILED);
		}		
	}
		
	public Result login() {
		Form<Account> form = formFactory.form(Account.class).bindFromRequest();
		try {
			session().clear();
			String username = form.data().get("username");
			String password = form.data().get("password");
			if (null == username || null == password) {
				throw new CodeException(ErrDefinition.E_ACCOUNT_INCORRECT_PARAM);
			}
			
			Account account = Account.find.where()
					.eq("username", username).findUnique();
			if (account == null) {
				throw new CodeException(ErrDefinition.E_ACCOUNT_NOT_FOUND);
			}
			
			if (account.password.compareTo(CodeGenerator.generateMD5(password)) != 0) {
				throw new CodeException(ErrDefinition.E_ACCOUNT_PASSWORD_MISMATCH);
			}
			
			if (!account.isEnabled) {
				throw new CodeException(ErrDefinition.E_ACCOUNT_DISABLED);
			}
			
			session("userId", account.id);
			session("role", account.roleType.toString());
	        ObjectNode node = Json.newObject();
	        node.put("code", 0);
	        node.put("id", account.id);
	        node.put("teamId", account.teamId);
			return ok(node);
		}
		catch (CodeException ce) {
			Logger.error(ce.getMessage());
			return failure(ce.getCode());
		}
		catch (Throwable e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			return failure(ErrDefinition.E_ACCOUNT_LOGIN_FAILED);
		}
	}
	
	public Result logout() {
		session().clear();
		
		return success(null, null);
	}
	
	public Result verifyMobile() {
		return ok();
	}
	
	@Security.Authenticated(Secured.class)
	public Result update() {
		Form<Account> form = formFactory.form(Account.class).bindFromRequest();
		
		try {
			if (form.hasErrors()) {
				throw new CodeException(ErrDefinition.E_ACCOUNT_INCORRECT_PARAM);
			}
			
			Account tmp = form.get();			
			
			String userId = session("userId");
			if (userId == null) {
				throw new CodeException(ErrDefinition.E_ACCOUNT_INCORRECT_PARAM);				
			}
			
			Account accountToUpdate = Account.find.byId(userId);
			if (accountToUpdate == null) {
				throw new CodeException(ErrDefinition.E_ACCOUNT_INCORRECT_PARAM);				
			}
						
			Account account = Account.find.byId(tmp.id);
			if (account == null) {
				throw new CodeException(ErrDefinition.E_ACCOUNT_INCORRECT_PARAM);				
			}
			
			if (userId.compareTo(account.id) != 0) {
			    //not same person to update
			    if (accountToUpdate.roleType != Authority.E_SUPERADMIN) {
			        if (accountToUpdate.roleType != Authority.E_ADMIN) {
			            //a mortal, no rights
                        throw new CodeException(ErrDefinition.E_ACCOUNT_NO_RIGHT);
			        }
			        else {
			            if (account.roleType == Authority.E_ADMIN) {
			                //same level, no rights
	                        throw new CodeException(ErrDefinition.E_ACCOUNT_NO_RIGHT);
			            }
			            else {
			                //change a mortal, oh yeah~
			            }
			        }
			    }
			    else {
			        //super admin, do everything like god
			    }
			}
			
					
			if (tmp.nicname != null) {
				account.nicname = tmp.nicname;
			}
			
			if (tmp.portrait != null) {
				account.portrait = tmp.portrait;
			}
			
			if (tmp.sex != null) {
				account.sex = tmp.sex;
			}
			
			if (tmp.birthday != null) {
				account.birthday = tmp.birthday;
			}
			
			if (tmp.profession != null) {
				account.profession = tmp.profession;
			}
			
			if (tmp.mobile != null) {
				account.mobile = tmp.mobile;
			}

			if (tmp.introduction != null) {
				account.introduction = tmp.introduction;
			}
			
			if (tmp.email != null) {
				account.email = tmp.email;
			}

			if (tmp.roleType != null) {
				if(accountToUpdate.roleType == Authority.E_ADMIN ||
				   accountToUpdate.roleType == Authority.E_SUPERADMIN){
					account.roleType = tmp.roleType;
				}else {
					throw new CodeException(ErrDefinition.E_ACCOUNT_NO_RIGHT);
				}
			}
			
			if (tmp.isEnabled != account.isEnabled) {
				if(accountToUpdate.roleType == Authority.E_ADMIN ||
				   accountToUpdate.roleType == Authority.E_SUPERADMIN){
					account.isEnabled = tmp.isEnabled;
				}else {
					throw new CodeException(ErrDefinition.E_ACCOUNT_NO_RIGHT);
				}
			}
			
//			if (tmp.mobile != null) {
//				if (tmp.verifyCode != null) {
//					SmsRecord record = SmsRecord.find.byId(tmp.verifyCode);
//					if (record.mobile == Long.parseLong(tmp.mobile)) {
//						account.mobile = tmp.mobile;
//					}
//				}
//			}
			
			if (tmp.video != null) {
				account.video = tmp.video;
			}
			
			Ebean.update(account);
			
			return success(null, null);
		}
		catch (CodeException ce) {
			Logger.error(ce.getMessage());
			return failure(ce.getCode());			
		}
		catch (Throwable e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			return failure(ErrDefinition.E_ACCOUNT_UPDATE_FAILED);
		}
	}
	
	@Security.Authenticated(Secured.class)
	public Result read() {
		try {
			
			DynamicForm form = formFactory.form().bindFromRequest();			
			String userId = session("userId");
			
			Account account = Account.find.byId(userId);
			if (null == account) {
				throw new CodeException(ErrDefinition.E_ACCOUNT_NOT_FOUND);
			}				
			
			List<Account> accountList = null;
			if (account.roleType != Authority.E_ADMIN &&
			    account.roleType != Authority.E_SUPERADMIN) {
				accountList = new ArrayList<Account>();
				accountList.add(account);
				
				return successList(1, 1, accountList);
			}
			
			userId = form.get("id");
			
			if (userId != null && !userId.isEmpty()) {
				account = Account.find.byId(userId);
				if (null == account) {
					throw new CodeException(ErrDefinition.E_ACCOUNT_NOT_FOUND);
				}				
				
				accountList = new ArrayList<Account>();
				accountList.add(account);
				
				return successList(1, 1, accountList);
			}
			
			ExpressionList<Account> exprList = Account.find.where();
 			String pageStr = form.get("page");
			String numStr = form.get("num");
			
			if (null == pageStr || pageStr.isEmpty()) {
				throw new CodeException(ErrDefinition.E_ACCOUNT_INCORRECT_PARAM);
			}
			
			if (null == numStr || numStr.isEmpty()) {
				throw new CodeException(ErrDefinition.E_ACCOUNT_INCORRECT_PARAM);
			}
			
			Integer page = Integer.parseInt(pageStr);
			Integer num = Integer.parseInt(numStr);
			
			String username = form.get("username");
			
			if (username != null && !username.isEmpty()) {
				exprList.add(Expr.contains("username", username));
			}
			
			String roleType = form.get("roleType");
			
			if (roleType != null && !roleType.isEmpty()) {
				exprList.add(Expr.eq("roleType", Integer.parseInt(roleType)));
			}
			
			accountList = exprList
					.setFirstRow((page-1)*num)
					.setMaxRows(num)					
					.orderBy("createTime desc")
					.findList();
					
			int totalNum = exprList.findRowCount();			
			int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;
					
			return successList(totalNum, totalPage, accountList);
		}
		catch (CodeException ce) {
			Logger.error(ce.getMessage());
			return failure(ce.getCode());
		}
		catch (Throwable e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			return failure(ErrDefinition.E_ACCOUNT_READ_FAILED);
		}
	}
	
	@Security.Authenticated(Secured.class)
	public Result reset() {
		try {
			DynamicForm form = formFactory.form().bindFromRequest();
			String id = form.get("id"); //account id to reset
			
			Account account = Account.find.byId(session("userId"));
			
			if (account.roleType != Authority.E_ADMIN &&
			    account.roleType != Authority.E_SUPERADMIN) {
				throw new CodeException(ErrDefinition.E_ACCOUNT_NO_RIGHT);
			}
			
			
			Account accountToReset = Account.find.byId(id);
			
            if (accountToReset.roleType == Authority.E_SUPERADMIN) {
                if (session("userId").compareTo(accountToReset.id) != 0) {
                    throw new CodeException(ErrDefinition.E_ACCOUNT_NO_RIGHT);                  
                }
            }
			
			if (accountToReset == null) {
				throw new CodeException(ErrDefinition.E_ACCOUNT_NOT_FOUND);
			}
			
			accountToReset.password = CodeGenerator.generateMD5("123456");
			
			Ebean.update(accountToReset);
			return success();
		}
		catch (CodeException ce) {
			Logger.error(ce.getMessage());
			return failure(ce.getCode());
		}
		catch (Throwable e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			return failure(ErrDefinition.E_ACCOUNT_UPDATE_FAILED);
		}
	}

	@Security.Authenticated(Secured.class)
	public Result enable() {
		try {
			DynamicForm form = formFactory.form().bindFromRequest();
			String id = form.get("id"); //account id to reset
			String enable = form.get("enable");
			
			if (id == null || id.isEmpty() || enable == null || enable.isEmpty()) {
				throw new CodeException(ErrDefinition.E_ACCOUNT_INCORRECT_PARAM);
			}

			Account account = Account.find.byId(session("userId"));
			
			if (account.roleType != Authority.E_ADMIN &&
			        account.roleType != Authority.E_SUPERADMIN) {
				throw new CodeException(ErrDefinition.E_ACCOUNT_NO_RIGHT);
			}
						
			Account accountToEnable = Account.find.byId(id);
			
			if (accountToEnable.roleType == 0) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_NO_RIGHT);			    
			}
			accountToEnable.isEnabled = Boolean.parseBoolean(enable);
			
			Ebean.update(accountToEnable);
			return success();
		}
		catch (CodeException ce) {
			Logger.error(ce.getMessage());
			return failure(ce.getCode());
		}
		catch (Throwable e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			return failure(ErrDefinition.E_ACCOUNT_UPDATE_FAILED);
		}
	}

	@Security.Authenticated(Secured.class)
	public Result password() {
		try {
			DynamicForm form = formFactory.form().bindFromRequest();

			String userId = session("userId");

			if (userId == null || userId.isEmpty()) {
				throw new CodeException(ErrDefinition.E_ACCOUNT_INCORRECT_PARAM);
			}

			String password = form.get("password");
			if (password == null || password.isEmpty()){
				throw new CodeException(ErrDefinition.E_ACCOUNT_INCORRECT_PARAM);
			}

			String oldPassword = form.get("oldPassword");
			if (oldPassword == null || oldPassword.isEmpty()){
				throw new CodeException(ErrDefinition.E_ACCOUNT_INCORRECT_PARAM);
			}

			Account account = Account.find.byId(session("userId"));
			

			if (account.password.compareTo(CodeGenerator.generateMD5(oldPassword)) != 0) {
				throw new CodeException(ErrDefinition.E_ACCOUNT_PASSWORD_MISMATCH);
			}
			
			account.password = CodeGenerator.generateMD5(password);
			
			Ebean.update(account);
			return success();
		}
		catch (CodeException ce) {
			Logger.error(ce.getMessage());
			return failure(ce.getCode());
		}
		catch (Throwable e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			return failure(ErrDefinition.E_ACCOUNT_UPDATE_FAILED);
		}
	}
	
}
