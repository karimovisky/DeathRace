package race.main.com.deadrace;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

/**
 * Created by Karim Omaya on 9/21/2015.
 */
public class User {

    private String username;
    ParseUser user;
    private final Context mContext;

    public User(Context mContext) {
        this.mContext = mContext;
    }

    protected String getPicture(){
        String imgUrl = "";
        user = ParseUser.getCurrentUser();
        ParseFile image = user.getParseFile("ImageUpload");
        if(image != null)
            imgUrl = image.getUrl();
        return imgUrl;
    }

    protected void addPicture (byte[] image){
        // Create the ParseFile
        ParseFile file = new ParseFile("Avatar.jpeg", image);
        // Upload the image into Parse Cloud
        file.saveInBackground();
        user = ParseUser.getCurrentUser();
        user.put("ImageUpload",file);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null){
                    Toast.makeText(mContext, "Profile pic Upload", Toast.LENGTH_LONG);
                }
                else {
                    Toast.makeText(mContext, "error in uploaded", Toast.LENGTH_LONG);
                }
            }
        });

    }

    protected void createUser(String username, String password, String cPassword){
        Toast.makeText(mContext, "enter function", Toast.LENGTH_LONG);
        boolean validate = validationEntery(username,password,cPassword);
        if (validate) {
            Toast.makeText(mContext, "validate complete", Toast.LENGTH_LONG);
            user = new ParseUser();
            user.setUsername(username);
            user.setPassword(password);
            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG);

                    } else {
                        Intent intent = new Intent(mContext, HomeActivity.class);
                        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TASK | intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    }
                }
            });
        }
        else {
            Toast.makeText(mContext, "Validate your entery", Toast.LENGTH_LONG);
        }

    }

    protected void login(String uname, String pass){
        ParseUser.logInInBackground(uname, pass, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e == null){
                    Intent intent = new Intent(mContext, HomeActivity.class);
                    intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TASK | intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
                else {
                    Toast.makeText(mContext, e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    protected void logout(){
        ParseUser.logOut();
        Intent intent = new Intent(mContext, DispatchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private boolean validationEntery(String username,String password, String cPassword){
        boolean validationError = false;

        if (username.length() == 0){
            validationError = true;
        }

        if (password.length() == 0){
            if (validationError) {
            }
            validationError = true;
        }

        if(!password.equals(cPassword)){
            if(validationError){
            }
            validationError = true;
        }

        if(validationError){
            Toast.makeText(mContext,"Check your entery",Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private String getUsername(){
        user = ParseUser.getCurrentUser();
        username = user.getUsername();
        return username;
    }
}
