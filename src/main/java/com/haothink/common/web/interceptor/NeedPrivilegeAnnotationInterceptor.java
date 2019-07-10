package com.haothink.common.web.interceptor;

import com.haothink.common.web.annotation.NeedPrivilege;
import com.haothink.common.web.session.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * 对使用Login注解的类进行拦截，正对不同设置进行相应的返回
 * 对于正常表单提交（非AJAX）跳转到登录页面，对于ajax提交的请求返回ajax数据
 * @author wanghao
 */
public class NeedPrivilegeAnnotationInterceptor extends HandlerInterceptorAdapter {

	@Autowired
	SessionService sessionService;
	@Autowired
	String errPageUrl;

	@Override
	public boolean preHandle(HttpServletRequest request,HttpServletResponse response, Object handler) throws Exception {
		HandlerMethod handler2 = (HandlerMethod) handler;
		NeedPrivilege needPrivilege = handler2.getMethodAnnotation(NeedPrivilege.class);
		if (null == needPrivilege) {
			return true;
		}

		Object obj = sessionService.getSession(request);
		if (null == obj) {
			returnMessage(request, response, needPrivilege);
			return false;
		}else{
			Set<Integer> userPrivilegeSet=(Set<Integer>)obj;
			if(userPrivilegeSet.contains(needPrivilege.privilegeSid())){
				return true;
			}else{
				returnMessage(request, response, needPrivilege);
				return false;
			}
		}
	}

	private void returnMessage(HttpServletRequest request,
			HttpServletResponse response, NeedPrivilege needPrivilege)
			throws ServletException, IOException {
			// 传统页面的登录
			request.getRequestDispatcher(errPageUrl).forward(request,response);
	}

	public SessionService getSessionService() {
		return sessionService;
	}

	public void setSessionService(SessionService sessionService) {
		this.sessionService = sessionService;
	}
}
