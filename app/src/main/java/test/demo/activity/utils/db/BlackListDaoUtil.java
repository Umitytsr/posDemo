package test.demo.activity.utils.db;



import com.ctk.sdk.DebugLogUtil;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import test.demo.activity.db.dao.BlackListDao;
import test.demo.activity.db.dao.DBSessionManager;
import test.demo.activity.db.entiry.BlackList;

public class BlackListDaoUtil {

    private static final String TAG = BlackListDaoUtil.class.getSimpleName();
    private BlackListDao dao;

    public BlackListDaoUtil() {
        dao = (BlackListDao) DBSessionManager.getDaoSession().getBlackListDao();
    }

    /**
     * 插入多个黑名单
     *
     * @param blackLists
     * @return
     */
    public boolean insertMultBlackList(List<BlackList> blackLists) {
        boolean flag = false;
        try {
            for (BlackList blackList : blackLists) {
                dao.insertOrReplace(blackList);
            }
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 根据Pan查询
     *
     * @param pan
     * @return
     */
    public List<BlackList> queryBlackListByPan(String pan) {
        List<BlackList> list = null;
        try {
            QueryBuilder<BlackList> queryBuilder = dao.queryBuilder();
            list = queryBuilder.where(BlackListDao.Properties.Tag5A.eq(pan)).build().list();
        } catch (Exception e) {
            DebugLogUtil.e(TAG, e.getMessage());
        }
        return list;
    }

    /**
     * 判断此卡是否在黑名单中
     *
     * @param pan
     * @param sn
     * @return
     */
    public boolean isBlackList(String pan, String sn) {
        List<BlackList> list = queryBlackListByPan(pan);
        for (BlackList blackList : list) {
            if (sn.equals(blackList.getTag5F34())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 删除所有数据
     *
     * @return
     */
    public boolean deleteAll() {
        boolean flag = false;
        try {
            dao.deleteAll();
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }


}
