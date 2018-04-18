package com.akaxin.admin.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.akaxin.admin.service.IUserService;
import com.akaxin.common.utils.StringHelper;
import com.akaxin.proto.core.PluginProto;
import com.akaxin.proto.core.UserProto.UserStatus;
import com.akaxin.site.storage.bean.SimpleUserBean;
import com.akaxin.site.storage.bean.UserProfileBean;

/**
 * 后台管理-用户管理
 */
@Controller
@RequestMapping("user")
public class UserManageController extends AbstractController {
	private static final Logger logger = LoggerFactory.getLogger(UserManageController.class);

	@Resource(name = "userManageService")
	private IUserService userService;

	// index.html 分页获取用户列表
	@RequestMapping("/index")
	public String toUserIndex() {
		return "/user/index";
	}

	// 用户个人资料展示界面，此界面编辑用户资料，并执行更新
	@RequestMapping("/profile")
	public ModelAndView toUserProfile(HttpServletRequest request, @RequestBody byte[] bodyParam) {
		ModelAndView modelAndView = new ModelAndView("user/profile");

		try {
			PluginProto.ProxyPluginPackage pluginPackage = PluginProto.ProxyPluginPackage.parseFrom(bodyParam);
			String currentUserId = getRequestSiteUserId(pluginPackage);

			if (isManager(currentUserId)) {
				Map<String, String> reqMap = getRequestDataMap(pluginPackage);
				String siteUserId = reqMap.get("site_user_id");

				UserProfileBean bean = userService.getUserProfile(siteUserId);
				modelAndView.addObject("siteUserId", bean.getSiteUserId());
				modelAndView.addObject("userName", bean.getUserName());
				modelAndView.addObject("userPhoto", bean.getUserPhoto());
				modelAndView.addObject("userIntroduce", bean.getSelfIntroduce());
				modelAndView.addObject("userStatus", bean.getUserStatus());
				modelAndView.addObject("regTime", bean.getRegisterTime());

			} else {
				return new ModelAndView("error");
			}
		} catch (Exception e) {
			logger.error(StringHelper.format("siteUserId={} get user profile error"), e);
		}

		return modelAndView;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/userList")
	@ResponseBody
	public Map<String, Object> getSiteUsers(HttpServletRequest request, @RequestBody byte[] bodyParam) {
		Map<String, Object> results = new HashMap<String, Object>();
		boolean nodata = true;

		try {
			PluginProto.ProxyPluginPackage pluginPackage = PluginProto.ProxyPluginPackage.parseFrom(bodyParam);
			String siteUserId = getRequestSiteUserId(pluginPackage);

			if (isManager(siteUserId)) {
				Map<String, String> dataMap = getRequestDataMap(pluginPackage);
				int pageNum = Integer.valueOf(dataMap.get("page"));

				List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
				List<SimpleUserBean> userList = userService.getUserList(pageNum, PAGE_SIZE);
				if (userList != null && userList.size() > 0) {
					if (PAGE_SIZE == userList.size()) {
						nodata = false;
					}
					for (SimpleUserBean bean : userList) {
						Map<String, Object> userMap = new HashMap<String, Object>();
						userMap.put("siteUserId", bean.getUserId());
						userMap.put("userName", bean.getUserName());
						userMap.put("userPhoto", bean.getUserPhoto());
						userMap.put("userStatus", bean.getUserStatus());

						data.add(userMap);
					}
				}

				results.put("userData", data);
			}

		} catch (Exception e) {
			logger.error("get site user list error", e);
		}

		results.put("loading", nodata);
		return results;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/updateProfile")
	@ResponseBody
	public String updateProfile(HttpServletRequest request, @RequestBody byte[] bodyParam) {
		try {
			PluginProto.ProxyPluginPackage pluginPackage = PluginProto.ProxyPluginPackage.parseFrom(bodyParam);
			String siteUserId = getRequestSiteUserId(pluginPackage);

			if (isManager(siteUserId)) {
				Map<String, String> reqMap = getRequestDataMap(pluginPackage);
				UserProfileBean bean = new UserProfileBean();
				bean.setSiteUserId(reqMap.get("siteUserId"));
				bean.setUserName(reqMap.get("userName"));
				bean.setUserPhoto(reqMap.get("userPhoto"));
				bean.setSelfIntroduce(reqMap.get("userIntroduce"));
				if (userService.updateProfile(bean)) {
					return SUCCESS;
				}
			} else {
				return NO_PERMISSION;
			}
		} catch (Exception e) {
			logger.error("update profile error", e);
		}
		return ERROR;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/sealup")
	@ResponseBody
	public String sealup(HttpServletRequest request, @RequestBody byte[] bodyParam) {
		try {
			PluginProto.ProxyPluginPackage pluginPackage = PluginProto.ProxyPluginPackage.parseFrom(bodyParam);
			String siteUserId = getRequestSiteUserId(pluginPackage);

			if (isManager(siteUserId)) {
				Map<String, String> reqMap = getRequestDataMap(pluginPackage);
				String reqStatus = reqMap.get("type");
				int status = UserStatus.NORMAL_VALUE;
				if ("1".equals(reqStatus)) {
					status = UserStatus.SEALUP_VALUE;
				}

				if (userService.sealUpUser(reqMap.get("site_user_id"), status)) {
					return SUCCESS;
				}
			} else {
				return NO_PERMISSION;
			}
		} catch (Exception e) {
			logger.error("update profile error", e);
		}
		return ERROR;
	}

}