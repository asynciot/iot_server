package models.accounts;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import play.data.format.Formats;
import play.data.validation.*;
import com.avaje.ebean.annotation.Index;

@Entity
public class Account extends Model {

	@Id
    @Column(length=16)
    public String id;

    @Constraints.MinLength(5)
    @Constraints.MaxLength(100)
    //@Constraints.Required
    @Column(nullable=false, length=100,unique=true)
    public String username;
    
    @JsonIgnore
    @Column(nullable=false)
    public String password;
    
    @Column(length=16, nullable=false)
    public String teamId;
        
	public String nicname;
	
	public String portrait;
	
	public Boolean sex;
	
    @Formats.DateTime(pattern="yyyy-MM-dd")
	public Date birthday;
    
    public String getBirthday() {
    	if (null != birthday) {
    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    		return sdf.format(birthday);
    	}
    	
    	return null;
    }
	
	public String profession;
	
	public String introduction;
	
	public String video;
		
	public String mobile;
	
	public String email;
    
	@Index
	@JsonIgnore
    @Column(nullable=false)
    public Date createTime;
    
    @Column(nullable=false)
    public Integer roleType;
    
    @Column(nullable=false)
    public Boolean isEnabled = true;
    
    public static Find<String, Account> find = 
    		new Find<String, Account>(){};
}
