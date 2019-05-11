package services.accounts;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.avaje.ebean.Ebean;

import models.accounts.Account;
import controllers.common.CodeGenerator;
import play.Logger;
@Singleton
public class AccountInitializer {

    @Inject
    public AccountInitializer() {
        
        if (Ebean.find(Account.class).findRowCount() == 0) {
            try {
                Account superAdmin = new Account();
                superAdmin.id = CodeGenerator.generateShortUUId();
                superAdmin.username = "admin";
                superAdmin.nicname = "系统管理员";
                superAdmin.password = CodeGenerator.generateMD5("adminadmin");
                superAdmin.createTime = new Date();
                superAdmin.birthday = new Date();
                superAdmin.roleType = 0;
                superAdmin.isEnabled = true;
                
                Ebean.save(superAdmin);
                Logger.info("create super admin successfully.");
            }
            catch (Throwable e) {
                e.printStackTrace();
                Logger.error("fail to create super admin.");
            }
        }
    }
}
