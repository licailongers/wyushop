package com.wyu.web.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;

import com.wyu.domain.User;
import com.wyu.service.UserService;
import com.wyu.utils.CommonsUtils;

public class UserServlet extends BaseServlet {

	// 判断用户是否已经存在
	public void checkUsername(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		// 获取数据
		String username = request.getParameter("username");

		// 查询用户是否存在
		UserService service = new UserService();
		boolean isExist = service.checkUsername(username);

		String json = "{\"isExist\":" + isExist + "}";
		response.getWriter().write(json);
	}

	// 注册register---用户注册
	public void register(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		// 验证码校验
		// 获得页面输入的验证
		String checkCode_client = request.getParameter("checkCode");
		// 获得生成图片的文字的验证码
		String checkCode_session = (String) request.getSession().getAttribute("checkcode_session");
		// 比对页面的和生成图片的文字的验证码是否一致
		if (!checkCode_session.equals(checkCode_client)) {
			request.setAttribute("loginInfo", "您的验证码不正确!");
			request.getRequestDispatcher("/register.jsp").forward(request, response);
			return;
		}

		// 获取数据
		Map<String, String[]> properties = request.getParameterMap();
		User user = new User();

		// 封装数据
		try {
			BeanUtils.populate(user, properties);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}

		user.setUid(CommonsUtils.getUUID());
		user.setTelephone(null);

		// 传递数据
		UserService service = new UserService();
		boolean isRegisterSuccess = service.regist(user);

		// 判断注册是否成功
		if (isRegisterSuccess) {
			// 跳转到注册成功页面
			response.sendRedirect(request.getContextPath() + "/login.jsp");
		} else {
			// 跳转到注册失败页面
			response.sendRedirect(request.getContextPath() + "/registerFail.jsp");
		}
	}

	// 登录login---用户登录
	public void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();

		// 验证码校验
		// 获得页面输入的验证
		String checkCode_client = request.getParameter("checkCode");
		// 获得生成图片的文字的验证码
		String checkCode_session = (String) request.getSession().getAttribute("checkcode_session");
		// 比对页面的和生成图片的文字的验证码是否一致
		if (!checkCode_session.equals(checkCode_client)) {
			request.setAttribute("loginInfo", "您的验证码不正确!");
			request.getRequestDispatcher("/login.jsp").forward(request, response);
			return;
		}

		// 获取用户名、密码
		String username = request.getParameter("username");
		String password = request.getParameter("password");

		// 传递数据
		UserService service = new UserService();
		User user = service.checkUser(username, password);
		// 判断登录是否成功
		if (user != null) {
			// 判断用户是否勾选自动登录
			String autoLogin = request.getParameter("autoLogin");
			if (autoLogin != null) {
				// 对中文张三进行编码
				String username_code = URLEncoder.encode(username, "UTF-8");// %AE4%kfj

				Cookie cookie_username = new Cookie("cookie_username", username_code);
				Cookie cookie_password = new Cookie("cookie_password", password);
				// 设置cookie的持久化时间
				cookie_username.setMaxAge(60 * 60);
				cookie_password.setMaxAge(60 * 60);
				// 设置cookie的携带路径
				cookie_username.setPath(request.getContextPath());
				cookie_password.setPath(request.getContextPath());
				// 发送cookie
				response.addCookie(cookie_username);
				response.addCookie(cookie_password);
			}
			// 跳转到首页
			session.setAttribute("user", user);
			response.sendRedirect(request.getContextPath());
		} else {
			// 跳转到登录失败页面
			request.setAttribute("checkInfo", "您的用户名或密码错误!");
			request.getRequestDispatcher("/login.jsp").forward(request, response);
		}
	}

	// 用户退出登录loginout
	public void loginOut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		//删除session域中的user
		session.removeAttribute("user");
		
		//覆盖之前自动登录的cookie---将存储在客户端的cookie删除
		Cookie cookie_username = new Cookie("cookie_username","");
		Cookie cookie_password = new Cookie("cookie_password","");
		//设置cookie的持久化时间
		cookie_username.setMaxAge(0);
		cookie_password.setMaxAge(0);
		//发送cookie
		response.addCookie(cookie_username);
		response.addCookie(cookie_password);
		
		response.sendRedirect(request.getContextPath()+"/login.jsp");
	}
}
