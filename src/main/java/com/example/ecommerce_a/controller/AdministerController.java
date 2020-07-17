package com.example.ecommerce_a.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import com.example.ecommerce_a.domain.Item;
import com.example.ecommerce_a.domain.Order;
import com.example.ecommerce_a.domain.Topping;
import com.example.ecommerce_a.domain.User;
import com.example.ecommerce_a.form.InsertItemForm;
import com.example.ecommerce_a.form.InsertToppingForm;
import com.example.ecommerce_a.form.OrderSelectForm;
import com.example.ecommerce_a.service.AdministerService;
import com.example.ecommerce_a.service.ShowItemListService;

/**
 * 管理者のコントローラークラス.
 * 
 * @author hiroto.kitamura
 *
 */
@Controller
@RequestMapping("administer")
public class AdministerController {
	@Autowired
	private AdministerService adminService;
	@Autowired
	private ShowItemListService itemService;
	@Autowired
	private HttpSession session;

	@ModelAttribute
	private InsertItemForm setUpItemForm() {
		return new InsertItemForm();
	}

	@ModelAttribute
	private InsertToppingForm setUpToppingForm() {
		return new InsertToppingForm();
	}

	@ModelAttribute
	private OrderSelectForm setUpSelectForm() {
		return new OrderSelectForm();
	}

	/**
	 * 管理者のトップ画面を表示する.
	 * 
	 * @return 管理者トップ画面
	 */
	@RequestMapping("")
	public String index() {
		User user = (User) session.getAttribute("user");
		if (user == null || user.getId() != 0) {
			return "redirect:/toLogin";
		}
		return "administer";
	}

	/**
	 * 商品登録画面を表示する.
	 * 
	 * @return 商品登録画面
	 */
	@RequestMapping("toRegisterItem")
	public String toRegisterItem() {
		User user = (User) session.getAttribute("user");
		if (user == null || user.getId() != 0) {
			return "redirect:/toLogin";
		}
		return "register_item";
	}

	/**
	 * 商品を追加する.
	 * 
	 * @param form   商品のフォーム.
	 * @param result 入力値チェック結果
	 * @return 管理者トップ画面
	 * @throws IOException
	 */
	@RequestMapping("registerItem")
	public String registerItem(@Validated InsertItemForm form, BindingResult result) throws IOException {
		User user = (User) session.getAttribute("user");
		if (user == null || user.getId() != 0) {
			return "redirect:/toLogin";
		}
		MultipartFile image = form.getImage();
		try {
			String fileName = image.getOriginalFilename();
			if (!fileName.endsWith(".jpg") && !fileName.endsWith(".png")) {
				result.rejectValue("image", "", "拡張子は.jpgか.pngのみに対応しています");
			}
		} catch (Exception e) {
			result.rejectValue("image", "", "拡張子は.jpgか.pngのみに対応しています");
		}
		if (result.hasErrors()) {
			return "register_item";
		}
		adminService.insertItem(form);
		return "redirect:/administer";
	}

	/**
	 * トッピング登録画面を表示する.
	 * 
	 * @return トッピング登録画面
	 */
	@RequestMapping("toRegisterTopping")
	public String toRegisterTopping() {
		User user = (User) session.getAttribute("user");
		if (user == null || user.getId() != 0) {
			return "redirect:/toLogin";
		}
		return "register_topping";
	}

	/**
	 * トッピングを追加する.
	 * 
	 * @param form   入力フォーム
	 * @param result 入力値チェック結果
	 * @return 管理者トップ画面
	 */
	@RequestMapping("registerTopping")
	public String registerTopping(@Validated InsertToppingForm form, BindingResult result) {
		User user = (User) session.getAttribute("user");
		if (user == null || user.getId() != 0) {
			return "redirect:/toLogin";
		}
		if (result.hasErrors()) {
			return "register_topping";
		}
		adminService.insertTopping(form);
		return "redirect:/administer";
	}

	/**
	 * 検索ワードから商品を検索して商品削除画面を表示する.
	 * 
	 * @param name  検索ワード
	 * @param order 表示順(デフォルトでは価格の安い順になるように設定)
	 * @param model リクエストスコープに値を格納するためのオブジェクト
	 * @return 商品削除画面
	 */
	@RequestMapping("toDeleteItem")
	public String showItemList(String name, OrderSelectForm orderform, Model model) {
		User user = (User) session.getAttribute("user");
		if (user == null || user.getId() != 0) {
			return "redirect:/toLogin";
		}
		String order = orderform.getOrder();
		List<List<List<Item>>> itemListList = new ArrayList<>();
		String[] messageList = new String[3];
		for (int i = 0; i < 3; i++) {
			itemListList.add(adminService.show3colItemList(name, order, i));
			if (itemListList.get(i).size() == 0) {
				messageList[i] = "該当する商品がありません";
			}
		}
		String[] statusList = { "販売中の商品", "売り切れ中の商品", "販売停止中の商品" };
		model.addAttribute("statusList", statusList);
		model.addAttribute("message", messageList);
		// オートコンプリート用にJavaScriptの配列の中身を文字列で作ってスコープへ格納
		List<Item> showItemList = itemService.showItemList(name, order);
		StringBuilder itemListForAutocomplete = itemService.getItemListForAutocomplete(showItemList);
		model.addAttribute("itemListForAutocomplete", itemListForAutocomplete);

		model.addAttribute("itemListList", itemListList);
		return "delete_item";
	}

	/**
	 * 商品の販売状況を変更する.
	 * 
	 * @param id     商品ID
	 * @param status 販売状況
	 * @return 商品削除画面
	 */
	@RequestMapping("deleteItem")
	public String deleteItem(Integer id, Integer status) {
		User user = (User) session.getAttribute("user");
		if (user == null || user.getId() != 0) {
			return "redirect:/toLogin";
		}
		adminService.setStatus(id, status);
		return "redirect:/administer/toDeleteItem";
	}

	/**
	 * トッピング削除画面を表示する.
	 * 
	 * @return トッピング削除画面
	 */
	@RequestMapping("toDeleteTopping")
	public String toDeleteTopping(Model model) {
		User user = (User) session.getAttribute("user");
		if (user == null || user.getId() != 0) {
			return "redirect:/toLogin";
		}
		List<List<Topping>> toppingListList = adminService.searchAllToppings();
		String[] messageList = new String[2];
		for (int i = 0; i < 2; i++) {
			if (toppingListList.get(i).size() == 0) {
				messageList[i] = "該当するトッピングがありません";
			}
		}
		String[] statusList = { "販売中のトッピング", "販売停止中のトッピング" };
		model.addAttribute("statusList", statusList);
		model.addAttribute("message", messageList);
		model.addAttribute("toppingListList", toppingListList);
		return "delete_topping";
	}

	/**
	 * トッピングの削除フラグを変更する.
	 * 
	 * @param id      トッピングID
	 * @param deleted 削除フラグ
	 * @return トッピング削除画面
	 */
	@RequestMapping("deleteTopping")
	public String deleteTopping(Integer id, Boolean deleted) {
		User user = (User) session.getAttribute("user");
		if (user == null || user.getId() != 0) {
			return "redirect:/toLogin";
		}
		adminService.setDeleted(id, deleted);
		return "redirect:/administer/toDeleteTopping";
	}

	/**
	 * 注文状態変更画面を表示する.
	 * 
	 * @return 注文状態変更画面
	 */
	@RequestMapping("toEditStatus")
	public String toEditStatus(Model model) {
		User user = (User) session.getAttribute("user");
		if (user == null || user.getId() != 0) {
			return "redirect:/toLogin";
		}
		List<List<Order>> orderListList = new ArrayList<>();
		orderListList.add(new ArrayList<Order>());
		String[] messageList = new String[4];
		for (int status = 1; status <= 3; status++) {
			List<Order> orderList = adminService.searchByStatus(status);
			orderListList.add(orderList);
			if (orderList == null || orderList.size() == 0 || orderList.get(0) == null) {
				messageList[status] = "注文がありません";
			}
		}
		String[] statusList = { null, "未入金", "入金済", "発送済" };
		model.addAttribute("orderListList", orderListList);
		model.addAttribute("messageList", messageList);
		model.addAttribute("statusList", statusList);
		return "edit_status";
	}

	/**
	 * 指定の注文IDのステータスを変更する.
	 * 
	 * @param orderId 注文ID
	 * @param status  変更後の状態
	 * @return
	 */
	@RequestMapping("editStatus")
	public String editStatus(Integer orderId, Integer status) {
		User user = (User) session.getAttribute("user");
		if (user == null || user.getId() != 0) {
			return "redirect:/toLogin";
		}
		adminService.editStatus(orderId, status);
		return "redirect:/administer/toEditStatus";
	}
}
