package com.example.accessingdatamysql;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.squareup.okhttp.*;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

@Controller	// This means that this class is a Controller
@RequestMapping(path="/") // This means URL's start with /demo (after Application path)
public class MainController {
	@Autowired // This means to get the bean called userRepository
			   // Which is auto-generated by Spring, we will use it to handle the data
	private UserRepository userRepository;
	
	@Value("${accesskey}")
    String accesskey;
    @Value("${secretkey}")
    String secretkey;
    @Value("${bucketName}")
    String bucketName;
	
	@GetMapping(path="/")
	public ModelAndView biotext() throws IOException, ParseException{
		
		User profile = userRepository.findByUsername("erik");
		
		String biotext = profile.getBio();
		String profilepic = profile.getProfpic();
		String name = profile.getName();
		
		ModelAndView view = new ModelAndView("home");
		
		view.addObject("biotext", biotext);
		view.addObject("profilepic", profilepic);
		view.addObject("name", name);
		
		OkHttpClient client = new OkHttpClient();

		Request request = new Request.Builder()
			.url("https://weatherbit-v1-mashape.p.rapidapi.com/current?lon=-73.823884&lat=42.686193")
			.get()
			.addHeader("x-rapidapi-key", "8516099479msh3520cf882ac6685p1e2a11jsn727d6a81970d")
			.addHeader("x-rapidapi-host", "weatherbit-v1-mashape.p.rapidapi.com")
			.build();

		Response response = client.newCall(request).execute();

		JSONParser p = new JSONParser();
		Object json = p.parse(response.body().string());
		JSONObject o = (JSONObject) json;
		JSONArray array = (JSONArray) o.get("data");
		
		String temperature = new String();
		
		for (int i = 0; i < array.size(); i++) {
			JSONObject temp = (JSONObject) array.get(i);
			temperature = (String) temp.get("temp");
		}
		
		view.addObject("temperature", temperature);
		
		return view;
	}
	
	@GetMapping(path="/login")
	public ModelAndView showLogin(){
		return new ModelAndView("login");
	}
	
	@GetMapping(path="/edit")
	public ModelAndView showEdit(){
		return new ModelAndView("edit");
	}
	
	@GetMapping(path="/confirm")
	public ModelAndView showConfirm(){
		return new ModelAndView("confirm");
	}
	
	@GetMapping(path="/validate")
	public ModelAndView showValidate(@RequestParam("username") String username, @RequestParam("password") String password){
		ModelAndView returnPage = new ModelAndView();
		User profile = userRepository.findByUsername(username);
		if ((profile.getUsername().equals(username)) && (profile.getPassword().equals(password))){
			returnPage.setViewName("edit");
		}
		else {
			returnPage.setViewName("error");
		}
		return returnPage;
	}
	
	@PostMapping(value = "/upload")
    public ModelAndView uploads3(@RequestParam("photo") MultipartFile image) {
        ModelAndView returnPage = new ModelAndView();

        BasicAWSCredentials cred = new BasicAWSCredentials(accesskey, secretkey);
        AmazonS3 client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(cred))
                .withRegion(Regions.US_EAST_1).build();
        try {
            PutObjectRequest put = new PutObjectRequest(bucketName, image.getOriginalFilename(),
                    image.getInputStream(), new ObjectMetadata()).withCannedAcl(CannedAccessControlList.PublicRead);
            client.putObject(put);

            String imgSrc = "http://" + bucketName + ".s3.amazonaws.com/" + image.getOriginalFilename();
			
			User update = userRepository.findByUsername("erik");
			update.setProfpic(imgSrc);
			userRepository.save(update);

            returnPage.setViewName("confirm");

            //Save this in the DB. 
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            returnPage.setViewName("error");
        }
        return returnPage;

    }
	
	@PostMapping(value = "/editprofile")
    public ModelAndView changeBio(@RequestParam("bio") String bio) {
        ModelAndView returnPage = new ModelAndView();

        try {			
			User update = userRepository.findByUsername("erik");
			update.setBio(bio);
			userRepository.save(update);

            returnPage.setViewName("confirm");

            //Save this in the DB. 
        } catch(Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            returnPage.setViewName("error");
        }
        return returnPage;
    }
	
	@PostMapping(value = "/editname")
    public ModelAndView changeName(@RequestParam("name") String name) {
        ModelAndView returnPage = new ModelAndView();

        try {			
			User update = userRepository.findByUsername("erik");
			update.setName(name);
			userRepository.save(update);

            returnPage.setViewName("confirm");

            //Save this in the DB. 
        } catch(Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            returnPage.setViewName("error");
        }
        return returnPage;
    }
	
	@GetMapping(path="/error")
	public ModelAndView showError(){
		return new ModelAndView("error");
	}
}
