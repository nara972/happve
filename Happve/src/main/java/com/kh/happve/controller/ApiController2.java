package com.kh.happve.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.happve.entity.Restaurant;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;


@Controller
public class ApiController2 {

	@RequestMapping("/api/basic2")
	public String basic(Model model) {
		StringBuffer result = new StringBuffer();
		Restaurant ra = null;
		JSONArray rowrow = null;
		try {
			StringBuilder urlBuilder = new StringBuilder("http://openapi.seoul.go.kr:8088"); /*URL 각팀별로 가져오려는 공공데이터 엔드포인트 주소 , 샘플-무더위쉼터 엔드포인트*/
			urlBuilder.append("/" + "6f4a424463716c773834794d516d44"); /*Service Key 공공데이터포털에서 받은 인증키*/
			urlBuilder.append("/" + URLEncoder.encode("json", "UTF-8")); /*호출문서 형태*/
			urlBuilder.append("/" + URLEncoder.encode("CrtfcUpsoInfo", "UTF-8")); /*서비스명*/
			urlBuilder.append("/" + 1); //*한 페이지 결과 수*/
			urlBuilder.append("/" + 5+"/"); /*페이지번호*/
			//urlBuilder.append("/" + 9657 + "/"); /*페이지번호*/

			URL url = new URL(urlBuilder.toString());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			BufferedReader rd;
			if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
				rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			} else {
				rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
			}
			String line;
			while ((line = rd.readLine()) != null) {
				result.append(line + "\n");
			}
			rd.close();
			conn.disconnect();

			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(result.toString());
			JSONObject CrtfcUpsoInfo = (JSONObject) jsonObject.get("CrtfcUpsoInfo");
			rowrow = (JSONArray) CrtfcUpsoInfo.get("row");

		} catch (Exception e) {
			e.printStackTrace();
		}

		model.addAttribute("rowrow",rowrow);

		return "index";

	}

	@GetMapping("/dispatcherView")
	private List<Restaurant> dispatcherView(JSONArray jsonArray) {
		List<Restaurant> list = new ArrayList<>();
		Restaurant restaurant = null;
		JSONObject arrayToJson = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			for(int i =0; i< jsonArray.size(); i++){
				arrayToJson = (JSONObject) jsonArray.get(i);
				System.out.println("arrayToJson ===> " + arrayToJson);
				restaurant = mapper.readValue(arrayToJson.toString(), Restaurant.class);
				list.add(restaurant);
				System.out.println("===== getCrtfc_upso_mgt_sno() ===> " + restaurant.getCrtfc_upso_mgt_sno());
			}

			System.out.println("list data ===> " + list);

		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}



		return list;
	}
}