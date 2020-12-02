package com.wyu.web.servlet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.wyu.domain.Category;
import com.wyu.domain.Order;
import com.wyu.domain.Product;
import com.wyu.service.AdminService;
import com.wyu.utils.CommonsUtils;

public class AdminServlet extends BaseServlet {

	// Order订单的业务操作：
	// 根据订单id查询订单项和商品信息
	public void findOrderInfoByOid(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String oid = request.getParameter("oid");

		AdminService service = new AdminService();
		List<Map<String, Object>> mapList = service.findOrderInfoByOid(oid);

		Gson gson = new Gson();
		String json = gson.toJson(mapList);
		System.out.println(json);
		/*
		 * [ {"shop_price":4499.0,"count":2,"pname":"联想（Lenovo）小新V3000经典版","pimage":
		 * "products/1/c_0034.jpg","subtotal":8998.0},
		 * {"shop_price":2599.0,"count":1,"pname":"华为 Ascend Mate7","pimage":
		 * "products/1/c_0010.jpg","subtotal":2599.0} ]
		 */
		response.setContentType("text/html;charset=UTF-8");
		response.getWriter().write(json);

	}

	// 获得所有的订单
	public void findAllOrders(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 获得所有的订单信息----List<Order>

		AdminService service = new AdminService();
		List<Order> orderList = service.findAllOrders();

		request.setAttribute("orderList", orderList);
		request.getRequestDispatcher("/admin/order/list.jsp").forward(request, response);
	}

	// 查找所有商品类别
	public void findAllCategory(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 提供一个List<Category> 转成json字符串
		AdminService service = new AdminService();
		List<Category> categoryList = service.findAllCategory();

		Gson gson = new Gson();
		String json = gson.toJson(categoryList);

		response.setContentType("text/html;charset=UTF-8");
		response.getWriter().write(json);
	}

	// Product订单的业务操作：
	// 根据订单id查询订单项和商品信息
	public void productList(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		AdminService service = new AdminService();
		List<Product> productList = service.findAllProduct();

		request.setAttribute("productList", productList);
		request.getRequestDispatcher("admin/product/list.jsp").forward(request, response);
	}

	// 根据订单pid删除该订单项
	public void delProduct(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String pid = request.getParameter("pid");

		AdminService service = new AdminService();
		service.delProductById(pid);

		response.sendRedirect(request.getContextPath() + "/admin?method=productList");
	}

	// 添加商品
	public void addProduct(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 目的：收集表单的数据 封装一个Product实体 将上传图片存到服务器磁盘上
		Product product = new Product();

		// 收集数据的容器
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			// 创建磁盘文件项工厂
			DiskFileItemFactory factory = new DiskFileItemFactory();
			// 创建文件上传核心对象
			ServletFileUpload upload = new ServletFileUpload(factory);
			// 解析request获得文件项对象集合

			List<FileItem> parseRequest = upload.parseRequest(request);
			for (FileItem item : parseRequest) {
				// 判断是否是普通表单项
				boolean formField = item.isFormField();
				if (formField) {
					// 普通表单项 获得表单的数据 封装到Product实体中
					String fieldName = item.getFieldName();
					String fieldValue = item.getString("UTF-8");

					map.put(fieldName, fieldValue);
				} else {
					// 文件上传项 获得文件名称 获得文件的内容
					String fileName = item.getName();
					String path = this.getServletContext().getRealPath("upload");
					InputStream in = item.getInputStream();
					OutputStream out = new FileOutputStream(path + "/" + fileName);// I:/xxx/xx/xxx/xxx.jpg
					IOUtils.copy(in, out);
					in.close();
					out.close();
					item.delete();

					map.put("pimage", "upload/" + fileName);
				}

			}

			BeanUtils.populate(product, map);
			// 是否product对象封装数据完全
			// private String pid;
			product.setPid(CommonsUtils.getUUID());
			// private Date pdate;
			// 转换时间格式
			SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String format = simpleDate.format(new Date());
			Date newdate = simpleDate.parse(format);
			product.setPdate(newdate);
			// private int pflag;
			product.setPflag(0);
			// private Category category;
			Category category = new Category();
			category.setCid(map.get("cid").toString());
			product.setCategory(category);

			// 将product传递给service层
			AdminService service = new AdminService();
			service.saveProduct(product);

			request.getRequestDispatcher("/admin?method=productList").forward(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 修改商品信息
	public void updateProduct(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 目的：收集表单的数据 封装一个Product实体 将上传图片存到服务器磁盘上
		Product product = new Product();

		// 收集数据的容器
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			// 创建磁盘文件项工厂
			DiskFileItemFactory factory = new DiskFileItemFactory();
			// 创建文件上传核心对象
			ServletFileUpload upload = new ServletFileUpload(factory);
			// 解析request获得文件项对象集合

			List<FileItem> parseRequest = upload.parseRequest(request);
			for (FileItem item : parseRequest) {
				// 判断是否是普通表单项
				boolean formField = item.isFormField();
				if (formField) {
					// 普通表单项 获得表单的数据 封装到Product实体中
					String fieldName = item.getFieldName();
					String fieldValue = item.getString("UTF-8");

					map.put(fieldName, fieldValue);
				} else {
					// 文件上传项 获得文件名称 获得文件的内容
					String fileName = item.getName();
					String path = this.getServletContext().getRealPath("upload");
					InputStream in = item.getInputStream();
					OutputStream out = new FileOutputStream(path + "/" + fileName);// I:/xxx/xx/xxx/xxx.jpg
					IOUtils.copy(in, out);
					in.close();
					out.close();
					item.delete();

					map.put("pimage", "upload/" + fileName);
				}
			}

			BeanUtils.populate(product, map);
			// 是否product对象封装数据完全
			// private Date pdate;
			// 转换时间格式
			SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String format = simpleDate.format(new Date());
			Date newdate = simpleDate.parse(format);
			product.setPdate(newdate);
			// private int pflag;
			product.setPflag(0);
			// private Category category;
			Category category = new Category();
			category.setCid(map.get("cid").toString());
			product.setCategory(category);
			
			// 将product传递给service层
			AdminService service = new AdminService();
			service.updateProduct(product);

			request.getRequestDispatcher("/admin?method=productList").forward(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 修改商品信息edit---根据pid获取该商品的所有信息
	public void updateProductUI(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String pid = request.getParameter("pid");

		AdminService service = new AdminService();
		Product product = service.findProductById(pid);

		List<Category> categoryList = service.findAllCategory();

		request.setAttribute("categoryList", categoryList);
		request.setAttribute("product", product);

		request.getRequestDispatcher("/admin/product/edit.jsp").forward(request, response);
	}

	// Category订单的业务操作：
	// 查找所有商品类别
	public void categoryList(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 提供一个List<Category> 转成json字符串
		AdminService service = new AdminService();
		List<Category> categoryList = service.findAllCategory();

		Gson gson = new Gson();
		String json = gson.toJson(categoryList);

		request.setAttribute("categoryList", categoryList);
		request.getRequestDispatcher("admin/category/list.jsp").forward(request, response);
	}

	// 添加商品类别
	public void addCategory(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String cname = request.getParameter("cname");
		Category category = new Category();
		category.setCid(CommonsUtils.getUUID());
		category.setCname(cname);

		AdminService service = new AdminService();
		service.addCategory(category);

		request.getRequestDispatcher("/admin?method=categoryList").forward(request, response);
	}

	// 根据商品类别ID删除该商品类别
	public void delCategoryByCid(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String cid = request.getParameter("cid");

		AdminService service = new AdminService();
		service.delCategoryByCid(cid);

		request.getRequestDispatcher("/admin?method=categoryList").forward(request, response);
	}

	// 根据cid查找category
	public void editCategoryUI(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String cid = request.getParameter("cid");

		AdminService service = new AdminService();
		Category category = service.findCategoryByCid(cid);

		request.setAttribute("category", category);
		request.getRequestDispatcher("/admin/category/edit.jsp").forward(request, response);
	}

	// 编辑category
	public void editCategory(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String cid = request.getParameter("cid");
		String cname = request.getParameter("cname");

		AdminService service = new AdminService();
		service.eidtCategory(cid, cname);

		response.sendRedirect(request.getContextPath() + "/admin?method=categoryList");
	}

}
