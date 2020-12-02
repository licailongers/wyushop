package com.wyu.web.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;

import com.wyu.domain.Cart;
import com.wyu.domain.CartItem;
import com.wyu.domain.Order;
import com.wyu.domain.OrderItem;
import com.wyu.domain.PageBean;
import com.wyu.domain.Product;
import com.wyu.domain.User;
import com.wyu.service.ProductService;
import com.wyu.utils.CommonsUtils;
import com.wyu.utils.PaymentUtil;

public class ProductServlet extends BaseServlet {

	// 首页index---获取热门商品和最新商品
	public void index(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ProductService service = new ProductService();

		// 获取热门商品
		List<Product> hotProductList = service.findHotProduct();

		// 获取最新商品
		List<Product> newProductList = service.findNewProduct();

		// 获取导航栏banner上的商品类别
		// CategoryService Cservice = new CategoryService();
		// List<Category> categoryList = Cservice.findCategory();

		// 把获取的数据(商品类别、热门商品、最新商品)放到request域中
		// request.setAttribute("categoryList", categoryList);
		request.setAttribute("hotProductList", hotProductList);
		request.setAttribute("newProductList", newProductList);

		request.getRequestDispatcher("/index.jsp").forward(request, response);
	}

	// 商品列表product_list---根据商品类别ID（cid）获取该cid的商品列表
	public void productListByCid(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 获取cid
		String cid = request.getParameter("cid");
		// 获取当前页面
		String currentPageStr = request.getParameter("currentPage");
		if (currentPageStr == null)
			currentPageStr = "1";
		int currentPage = Integer.parseInt(currentPageStr);
		int currentCount = 12;

		// 通过cid查找数据
		ProductService service = new ProductService();
		PageBean<Product> pageBean = service.findCategroyByCid(cid, currentPage, currentCount);

		request.setAttribute("cid", cid);
		request.setAttribute("pageBean", pageBean);

		// 定义一个记录历史商品信息的集合
		List<Product> historyProductList = new ArrayList<Product>();

		// 获得客户端携带名字叫pids的cookie
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("pids".equals(cookie.getName())) {
					String pids = cookie.getValue();
					String[] split = pids.split("-");
					for (String pid : split) {
						Product pro = service.findProductByPid(pid);
						historyProductList.add(pro);
					}
				}
			}
		}

		request.setAttribute("historyProductList", historyProductList);
		request.getRequestDispatcher("/product_list.jsp").forward(request, response);
	}

	// 商品详情product_info---根据商品ID（pid）获取该商品的详情页面
	public void productInfo(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		// 获取cid(商品类别)和当前页面
		String cid = request.getParameter("cid");
		String currentPage = request.getParameter("currentPage");

		// 获取pid
		String pid = request.getParameter("pid");

		ProductService service = new ProductService();
		Product product = service.findProductByPid(pid);

		request.setAttribute("cid", cid);
		request.setAttribute("currentPage", currentPage);
		request.setAttribute("product", product);

		// 获得客户端携带cookie---获得名字是pids的cookie
		String pids = pid;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("pids".equals(cookie.getName())) {
					pids = cookie.getValue();
					String[] split = pids.split("-");
					List<String> asList = Arrays.asList(split);
					LinkedList<String> list = new LinkedList<String>(asList);
					// 判断集合中是否存在当前pid
					if (list.contains(pid)) {
						// 包含当前查看商品的pid
						list.remove(pid);
						list.addFirst(pid);
					} else {
						// 不包含当前查看商品的pid
						list.addFirst(pid);
					}
					// 讲集合转成字符串
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < list.size() && i < 7; i++) {
						sb.append(list.get(i));
						sb.append("-");
					}
					// 去掉字符串后的-（拼凑后最后多出一个‘-’符号）
					pids = sb.substring(0, sb.length() - 1);
				}
			}
		}

		// 创建一个cookie存放pids
		Cookie cooke_pids = new Cookie("pids", pids);
		response.addCookie(cooke_pids);
		request.getRequestDispatcher("/product_info.jsp").forward(request, response);
	}

	// 购物车cart---添加商品到购物车
	public void addProductToCart(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();

		//获取user用来判断用户是否登录
		User user = (User) session.getAttribute("user");
		
		// 获取商品pid和购买数量
		String pid = request.getParameter("pid");
		int buyNum = Integer.parseInt(request.getParameter("buyNum"));

		// 获取商品信息
		ProductService service = new ProductService();
		Product product = service.findProductByPid(pid);

		// 计算小计
		double subTotal = product.getShop_price() * buyNum;

		// 封装CartItem---购物项
		CartItem cartItem = new CartItem();
		cartItem.setBuyNum(buyNum);
		cartItem.setProduct(product);
		cartItem.setSubTotal(subTotal);

		// 获得购物车Cart---查看购物车中是否有购物项
		Cart cart = (Cart) session.getAttribute("cart");
		if (cart == null) {
			cart = new Cart();
		}
		// 先判断购物车中是否已经包含此购物项
		// 如果购物车中已经存在该商品，将现在买的数量与原有的数量相加
		Map<String, CartItem> cartItems = cart.getCartItems();
		if (cartItems.containsKey(pid)) {
			// 取出原有的数量
			CartItem oldCartItem = cartItems.get(pid);
			int oldBuyNum = oldCartItem.getBuyNum();
			oldBuyNum += buyNum;
			oldCartItem.setBuyNum(oldBuyNum);
			// 修改小计
			double oldSubTotal = oldCartItem.getSubTotal();
			oldCartItem.setSubTotal(oldSubTotal + subTotal);
			cart.setCartItems(cartItems);
		} else {
			// 购物车中没有就将该购物项放入到购物车中
			cart.getCartItems().put(pid, cartItem);
		}

		// 计算总计
		double total = cart.getTotal() + subTotal;
		cart.setTotal(total);
		// 将购物车放入到session域中
		session.setAttribute("cart", cart);
		session.setAttribute("user", user);

		response.sendRedirect(request.getContextPath() + "/cart.jsp");
	}

	// 购物车cart---根据商品ID（pid）从购物车中删除该购物项
	public void delProFromCart(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 获取数据
		String pid = request.getParameter("pid");

		HttpSession session = request.getSession();
		Cart cart = (Cart) session.getAttribute("cart");

		if (cart != null) {
			Map<String, CartItem> cartItems = cart.getCartItems();
			// 修改总计
			cart.setTotal(cart.getTotal() - cartItems.get(pid).getSubTotal());
			// 删除购物项
			cartItems.remove(pid);
			cart.setCartItems(cartItems);
		}

		session.setAttribute("cart", cart);
		response.sendRedirect(request.getContextPath() + "/cart.jsp");
	}

	// 购物车cart---清空购物车
	public void clearCart(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		// 删除session域中的购物车
		session.removeAttribute("cart");
		response.sendRedirect(request.getContextPath() + "/cart.jsp");
	}

	// 订单详情order_info---提交订单
	public void submitOrder(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();

		// 判断用户是否已经登录
		User user = (User) session.getAttribute("user");
		if (user == null) {
			response.sendRedirect(request.getContextPath() + "/login.jsp");
			return;
		}

		// 目的：封装好一个Order对象 传递给service层
		Order order = new Order();
		order.setOid(CommonsUtils.getUUID()); // 该订单的订单号
		// 获取当前时间
		Date nowdate = new Date();
		// 转换时间格式
		SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		order.setOrdertime(Timestamp.valueOf(simpleDate.format(nowdate))); // 下单时间
		// 获得session中的购物车
		Cart cart = (Cart) session.getAttribute("cart");
		order.setTotal(cart.getTotal()); // 该订单的总金额
		order.setState(0); // 订单支付状态 1代表已付款 0代表未付款
		order.setAddress(null); // 收货地址
		order.setName(user.getName()); // 收货人
		order.setTelephone(user.getTelephone()); // 收货人电话
		order.setUser(user); // 该订单属于哪个用户
		// 该订单中有多少订单项List<OrderItem> orderItems = new ArrayList<OrderItem>();
		// 获得购物车中的购物项的集合map
		Map<String, CartItem> cartItems = cart.getCartItems();
		for (Map.Entry<String, CartItem> entry : cartItems.entrySet()) {
			// 取出每一个购物项
			CartItem cartItem = entry.getValue();
			// 创建新的订单项
			OrderItem orderItem = new OrderItem();
			orderItem.setItemid(CommonsUtils.getUUID()); // 订单项的id
			orderItem.setCount(cartItem.getBuyNum()); // 订单项内商品的购买数量
			orderItem.setSubtotal(cartItem.getSubTotal()); // 订单项小计
			orderItem.setProduct(cartItem.getProduct()); // 订单项内部的商品
			orderItem.setOrder(order); // 该订单项属于哪个订单
			// 将该订单项添加到订单的订单项集合中
			order.getOrderItems().add(orderItem);
		}

		// order对象封装完成
		ProductService service = new ProductService();
		service.submitOrder(order);

		session.setAttribute("order", order);
		response.sendRedirect(request.getContextPath() + "/order_info.jsp");
	}

	// 确认订单---判断订单信息，例如金额等是否被修改过
	public void callback(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 获得回调所有数据
		String p1_MerId = request.getParameter("p1_MerId");
		String r0_Cmd = request.getParameter("r0_Cmd");
		String r1_Code = request.getParameter("r1_Code");
		String r2_TrxId = request.getParameter("r2_TrxId");
		String r3_Amt = request.getParameter("r3_Amt");
		String r4_Cur = request.getParameter("r4_Cur");
		String r5_Pid = request.getParameter("r5_Pid");
		// 订单编号
		String r6_Order = request.getParameter("r6_Order");
		String r7_Uid = request.getParameter("r7_Uid");
		String r8_MP = request.getParameter("r8_MP");
		String r9_BType = request.getParameter("r9_BType");
		String rb_BankId = request.getParameter("rb_BankId");
		String ro_BankOrderId = request.getParameter("ro_BankOrderId");
		String rp_PayDate = request.getParameter("rp_PayDate");
		String rq_CardNo = request.getParameter("rq_CardNo");
		String ru_Trxtime = request.getParameter("ru_Trxtime");
		// 身份校验 --- 判断是不是支付公司通知你
		String hmac = request.getParameter("hmac");
		String keyValue = ResourceBundle.getBundle("merchantInfo").getString("keyValue");

		// 自己对上面数据进行加密 --- 比较支付公司发过来hamc
		boolean isValid = PaymentUtil.verifyCallback(hmac, p1_MerId, r0_Cmd, r1_Code, r2_TrxId, r3_Amt, r4_Cur, r5_Pid,
				r6_Order, r7_Uid, r8_MP, r9_BType, keyValue);

		if (isValid) {
			// 响应数据有效
			if (r9_BType.equals("1")) {

				// 修改订单状态
				ProductService service = new ProductService();
				service.updateOrderState(r6_Order);

				// 浏览器重定向
				response.setContentType("text/html;charset=utf-8");
				response.getWriter().println("<h1>付款成功！等待商城进一步操作！等待收货...</h1>");
			} else if (r9_BType.equals("2")) {
				// 服务器点对点 --- 支付公司通知你
				System.out.println("付款成功！");
				// 修改订单状态 为已付款
				// 回复支付公司
				response.getWriter().print("success");
			}
		} else {
			// 数据无效
			System.out.println("数据被篡改！");
		}
	}

	// 确认订单---支付订单
	public void confirmOrder(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 更新收货人信息
		Map<String, String[]> properties = request.getParameterMap();
		Order order = new Order();
		try {
			// 封装数据
			BeanUtils.populate(order, properties);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}

		ProductService service = new ProductService();
		service.updateOrderData(order);

		// 在线支付
		// 只接入一个接口，这个接口已经集成所有的银行接口了 ，这个接口是第三方支付平台提供的
		// 接入的是易宝支付
		// 获得 支付必须基本数据
		String orderid = request.getParameter("oid");

		// String money = order.getTotal()+""; //支付金额
		String money = "0.01"; // 支付金额---由于不是真的买，所以这里固定0.01支付金额
		// 银行
		String pd_FrpId = request.getParameter("pd_FrpId");

		// 发给支付公司需要哪些数据
		String p0_Cmd = "Buy";
		String p1_MerId = ResourceBundle.getBundle("merchantInfo").getString("p1_MerId");
		String p2_Order = orderid;
		String p3_Amt = money;
		String p4_Cur = "CNY";
		String p5_Pid = "";
		String p6_Pcat = "";
		String p7_Pdesc = "";
		// 支付成功回调地址 ---- 第三方支付公司会访问、用户访问
		// 第三方支付可以访问网址
		String p8_Url = ResourceBundle.getBundle("merchantInfo").getString("callback");
		String p9_SAF = "";
		String pa_MP = "";
		String pr_NeedResponse = "1";
		// 加密hmac 需要密钥
		String keyValue = ResourceBundle.getBundle("merchantInfo").getString("keyValue");
		String hmac = PaymentUtil.buildHmac(p0_Cmd, p1_MerId, p2_Order, p3_Amt, p4_Cur, p5_Pid, p6_Pcat, p7_Pdesc,
				p8_Url, p9_SAF, pa_MP, pd_FrpId, pr_NeedResponse, keyValue);

		String url = "https://www.yeepay.com/app-merchant-proxy/node?pd_FrpId=" + pd_FrpId + "&p0_Cmd=" + p0_Cmd
				+ "&p1_MerId=" + p1_MerId + "&p2_Order=" + p2_Order + "&p3_Amt=" + p3_Amt + "&p4_Cur=" + p4_Cur
				+ "&p5_Pid=" + p5_Pid + "&p6_Pcat=" + p6_Pcat + "&p7_Pdesc=" + p7_Pdesc + "&p8_Url=" + p8_Url
				+ "&p9_SAF=" + p9_SAF + "&pa_MP=" + pa_MP + "&pr_NeedResponse=" + pr_NeedResponse + "&hmac=" + hmac;

		// 重定向到第三方支付平台
		response.sendRedirect(url);
	}

}
