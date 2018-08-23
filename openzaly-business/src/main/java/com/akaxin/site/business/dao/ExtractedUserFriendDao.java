package com.akaxin.site.business.dao;

import com.akaxin.site.storage.api.IFriendApplyDao;
import com.akaxin.site.storage.bean.ApplyFriendBean;
import com.akaxin.site.storage.bean.ApplyUserBean;
import com.akaxin.site.storage.service.FriendApplyDaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

class ExtractedUserFriendDao {
    private static final Logger logger = LoggerFactory.getLogger(ExtractedUserFriendDao.class);
    private IFriendApplyDao friendApplyDao = new FriendApplyDaoService();

    ExtractedUserFriendDao() {
    }

    boolean saveFriendApply(String siteUserId, String siteFriendId, String applyReason) {
        try {
            return friendApplyDao.saveApply(siteFriendId, siteUserId, applyReason);
        } catch (SQLException e) {
            ExtractedUserFriendDao.logger.error("friend apply error.", e);
        }
        return false;
    }

    ApplyFriendBean agreeApplyWithClear(String siteUserId, String siteFriendId, boolean isMaster) {
        ApplyFriendBean bean = null;
        try {
            bean = friendApplyDao.getApplyInfo(siteUserId, siteFriendId, isMaster);
            friendApplyDao.deleteApply(siteFriendId, siteUserId);
            friendApplyDao.deleteApply(siteUserId, siteFriendId);
        } catch (SQLException e) {
            ExtractedUserFriendDao.logger.error("agree apply friend with clear error", e);
        }
        return bean;
    }

    /**
     * 获取一个好友的申请总数
     *
     * @param siteUserId
     * @param siteFriendId
     * @return
     */
    int getApplyCount(String siteUserId, String siteFriendId) {
        int count = 0;
        try {
            count = friendApplyDao.getApplyCount(siteUserId, siteFriendId);
        } catch (SQLException e) {
            ExtractedUserFriendDao.logger.error("get apply user count error.", e);
        }
        return count;
    }

    /**
     * 获取用户当前的好友申请总数
     *
     * @param siteUserId
     * @return
     */
    int getApplyCount(String siteUserId) {
        int count = 0;
        try {
            count = friendApplyDao.getApplyCount(siteUserId);
        } catch (SQLException e) {
            ExtractedUserFriendDao.logger.error("get apply user count error.", e);
        }
        return count;
    }

    List<ApplyUserBean> getApplyUserList(String siteUserId) {
        try {
            return friendApplyDao.getApplyUsers(siteUserId);
        } catch (SQLException e) {
            ExtractedUserFriendDao.logger.error("get apply user list error.", e);
        }
        return null;
    }
}