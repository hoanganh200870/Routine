package vn.hcmut.routine.util;

public class Constants {

    public static final String HOST = "http://api.routine.geekup.vn/";

    public static final String API_USERS = HOST + "users";
    public static final String PARAM_DEVICE_ID = "deviceId";
    public static final String JSON_USER_ID = "id";

    public static final String getDataApi(int userId) {
        return HOST + "data/" + userId;
    }

    public static final String USER_ID = "user_id";

}
