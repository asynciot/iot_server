/** Author: Michael Wang
 * Date: 2015-06-26
 * Description: Security check of all the actions except account related.
 */
package inceptors.common;

import com.google.inject.Inject;

import controllers.common.ErrDefinition;
import play.Logger;
import play.cache.CacheApi;
import play.libs.Json;
import play.mvc.*;
import play.mvc.Http.*;
import play.i18n.Messages;

public class Secured extends Security.Authenticator {

	@Inject
	private CacheApi cache;
	
    @Override
    public String getUsername(Context ctx) { 
//        String test = ctx.session().toString();
//        Logger.info(test);
//        Logger.info("cookies----input----"+ ctx.session().get("userId"));
    	String key = ctx.request().getQueryString("key");
    	if (key != null && !key.isEmpty()) {
    		String userId = cache.get(key);
    		if (userId != null && !userId.isEmpty()) {
    			ctx.session().put("userId", userId);
    		}
    	}
        return ctx.session().get("userId");
    }

    @Override
    public Result onUnauthorized(Context ctx) {
        return  ok(Json.newObject().put("code", ErrDefinition.E_ACCOUNT_UNAUTHENTICATED));
    }
}