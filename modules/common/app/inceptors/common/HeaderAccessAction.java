package inceptors.common;

import play.Logger;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Http.Session;
import play.mvc.Result;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import controllers.common.BaseController;
import controllers.common.ErrDefinition;

public class HeaderAccessAction extends Action.Simple{

	@Override
	public CompletionStage<Result> call(Context ctx) {
		// TODO Auto-generated method stub
        ctx = setRespHeader(ctx);
        Request request = ctx.request();
        String path = request.path(); //ctx._requestHeader().toString();
        String method = request.method();//(String) ctx.args.get("ROUTE_ACTION_METHOD");
              
        final String uuid = UUID.randomUUID().toString();
        final Long t0 = new Date().getTime();     
        Logger.info("id:"+ uuid + " | userId:" + ctx.session().get("userId") + " | method:" + method + "| path:" + path);
		
        final Context ctx1 = ctx;
        CompletionStage<Result> a = delegate.call(ctx).whenComplete((result, exception)->{
            Logger.info("id:" + uuid + " | userId:" + ctx1.session().get("userId") + " | time: " + Long.toString(new Date().getTime() - t0) + " | end");        	
            //return result;
        });        
        
        return a;
	}
	
//    @Override
//    public Promise<Result> call(Context ctx) throws Throwable {
//        // TODO Auto-generated method stub
//        ctx = setRespHeader(ctx);
//        Request request = ctx.request();
//        String path = request.path(); //ctx._requestHeader().toString();
//        String method = request.method();//(String) ctx.args.get("ROUTE_ACTION_METHOD");
//                
//        String uuid = UUID.randomUUID().toString();
//        Long t0 = new Date().getTime();     
//        Logger.info("id:"+ uuid +" | method:" + method + "| path:" + path);
//        
//        Promise<Result> result = null;
//        if (RequestManager.getInstance().handleRequest(ctx.session(), method, path)) {
//            result = Promise.pure(BaseController.staticFailure(ErrDefinition.E_DUPLICATED_REQUEST));
//        }
//        else {
//            try {            
//                result = delegate.call(ctx);
//            }
//            catch (Throwable e) {
//                e.printStackTrace();
//                result = Promise.pure(BaseController.staticFailure(ErrDefinition.E_INTERNAL_ERROR));
//            }            
//        }
//
//        RequestManager.getInstance().releaseRequest(ctx.session(), method, path);
//        Logger.info("id:" + uuid + " | time: " + Long.toString(new Date().getTime() - t0) + " | end");
//        
//        return result;
//    }
    
    private Context setRespHeader(Context context) {
        String header = context.request().getHeader("Origin");
        header = (header == null ? "*" : header);
        context.response().setHeader("Access-Control-Allow-Origin", header);
        context.response().setHeader("Access-Control-Allow-Credentials", "true");
        context.response().setHeader("Access-Control-Allow-Methods", "POST,GET,PUT,DELETE,OPTIONS");
        context.response().setHeader("Cache-Control", "no-cache");
        return context;
    }

}
