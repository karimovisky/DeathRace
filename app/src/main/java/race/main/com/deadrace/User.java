package race.main.com.deadrace;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.util.List;

/**
 * Created by Karim Omaya on 9/21/2015.
 */
public class User {

    private String username;
    ParseUser user;
    private final Context mContext;
    String userImage ="";

    public User(Context mContext) {
        this.mContext = mContext;
    }

    protected void showDialogWithPic(String uname) {

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", uname);
        try {
        List<ParseUser> results = query.find();

            for (ParseUser user : results) {
                ParseFile image = user.getParseFile("ImageUpload");
                if (image != null)
                    userImage = image.getUrl();
            }
        }
        catch (ParseException e){
            Toast.makeText(mContext,"Error in load the image", Toast.LENGTH_LONG).show();
        }


        ImageLoader imageLoader = ImageLoader.getInstance();
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(R.drawable.car)
                .showImageOnFail(R.drawable.car)
                .showImageOnLoading(R.drawable.car).build();

        String imgUrl = userImage;

        View view = LayoutInflater.from(mContext).inflate(R.layout.racebuilder, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext).setView(view).setTitle("Race");
        AlertDialog alert = builder.create();

        ImageView iv = (ImageView) view.findViewById(R.id.avatar);
        TextView tv = (TextView) view.findViewById(R.id.username);
        tv.setText(uname);

        imageLoader.displayImage(imgUrl, iv, options);
        alert.show();
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
                Toast.makeText(mContext,"Enter Valid Password", Toast.LENGTH_LONG).show();
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

    protected String getUsername(){
        user = ParseUser.getCurrentUser();
        username = user.getUsername();
        return username;
    }
}
