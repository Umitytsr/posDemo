package test.demo.activity.db.dao;


import android.database.sqlite.SQLiteDatabase;

import test.demo.MyApplication;
import test.demo.activity.db.dao.DaoMaster.DevOpenHelper;
import org.greenrobot.greendao.AbstractDao;

public final class DBSessionManager {
    private static final String DB_NAME = "emv_auth.db";
    private DaoSession daoSession;

    private DBSessionManager() {
        DevOpenHelper helper = new DevOpenHelper(MyApplication.app, DB_NAME);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster master = new DaoMaster(db);
        daoSession = master.newSession();
    }

    private static final class SingletonHolder {
        private static final DBSessionManager INSTANCE = new DBSessionManager();
    }

    public static DaoSession getDaoSession() {
        return SingletonHolder.INSTANCE.daoSession;
    }

    @SuppressWarnings("unchecked")
    public static <T extends AbstractDao<T, ?>> T getDao(Class<?> cls) {
        return (T) getDaoSession().getDao(cls);
    }

}
