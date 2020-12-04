package com.example.accessingdatamysql;

import org.junit.Test;
import org.junit.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import com.squareup.okhttp.*;

@SpringBootTest
public class MainControllerTest{
	
	@Test
	public void IsStateCorrect() throws IOException{
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
			.url("https://weatherbit-v1-mashape.p.rapidapi.com/current?lon=-73.823884&lat=42.686193")
			.get()
			.addHeader("x-rapidapi-key", "8516099479msh3520cf882ac6685p1e2a11jsn727d6a81970d")
			.addHeader("x-rapidapi-host", "weatherbit-v1-mashape.p.rapidapi.com")
			.build();

		Response response = client.newCall(request).execute();

		JsonObject myData = JsonParser.parseString(response.body().string()).getAsJsonObject();
        JsonArray dataArray = myData.getAsJsonArray("data");
        myData = dataArray.get(0).getAsJsonObject();
		String ret = myData.get("state_code").getAsString();

		Assert.assertEquals(ret, "NY");
	}

	@Test
	public void IsTempNotNull() throws IOException{
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
			.url("https://weatherbit-v1-mashape.p.rapidapi.com/current?lon=-73.823884&lat=42.686193")
			.get()
			.addHeader("x-rapidapi-key", "8516099479msh3520cf882ac6685p1e2a11jsn727d6a81970d")
			.addHeader("x-rapidapi-host", "weatherbit-v1-mashape.p.rapidapi.com")
			.build();

		Response response = client.newCall(request).execute();

		JsonObject myData = JsonParser.parseString(response.body().string()).getAsJsonObject();
        JsonArray dataArray = myData.getAsJsonArray("data");
        myData = dataArray.get(0).getAsJsonObject();
		String ret = myData.get("temp").getAsString();

		Assert.assertNotNull(ret);
	}
}