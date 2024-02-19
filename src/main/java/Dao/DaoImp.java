package Dao;

import org.springframework.stereotype.Component;

@Component("dao")
public class DaoImp implements IDao{
    public String getDate() {
        return new java.util.Date().toString();
    }
}
