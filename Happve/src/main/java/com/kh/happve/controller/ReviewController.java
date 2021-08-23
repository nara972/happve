package com.kh.happve.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import javax.validation.Valid;

import com.kh.happve.entity.Review;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.happve.dto.ReviewForm;
import com.kh.happve.entity.Member;
import com.kh.happve.entity.Restaurant;
import com.kh.happve.service.ReviewService;
import com.kh.happve.validator.CurrentAccount;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReviewController {
	
	private final ReviewService reviewService;

	@GetMapping("/{crtfc_upso_mgt_sno}/review")
	public String review(@PathVariable Integer crtfc_upso_mgt_sno, Model model, @CurrentAccount Member member) {
		
		StringBuffer result = new StringBuffer();
		Restaurant ra = new Restaurant();
		JSONArray rowrow = null;
		try {
			StringBuilder urlBuilder = new StringBuilder(
					"http://openapi.seoul.go.kr:8088"); /* URL 각팀별로 가져오려는 공공데이터 엔드포인트 주소 , 샘플-무더위쉼터 엔드포인트 */
			urlBuilder.append("/" + "6f4a424463716c773834794d516d44"); /* Service Key 공공데이터포털에서 받은 인증키 */
			urlBuilder.append("/" + URLEncoder.encode("json", "UTF-8")); /* 호출문서 형태 */
			urlBuilder.append("/" + URLEncoder.encode("CrtfcUpsoInfo", "UTF-8")); /* 서비스명 */
			urlBuilder.append("/" + 1); // *한 페이지 결과 수*/
			urlBuilder.append("/" + 5); /* 페이지번호 */
			urlBuilder.append("/" + crtfc_upso_mgt_sno + "/"); /* 페이지번호 */

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

			JSONObject arrayToJson = null;
			ObjectMapper mapper = new ObjectMapper();

			/* Restaurant getOne */

			for (int i = 0; i < rowrow.size(); i++) {
				arrayToJson = (JSONObject) rowrow.get(i);
				System.out.println("arrayToJson ===> " + arrayToJson);
				ra = mapper.readValue(arrayToJson.toString(), Restaurant.class);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		model.addAttribute("ra", ra);
		model.addAttribute(new ReviewForm());
		
		return "writereview";
	}

	@PostMapping("/{crtfc_upso_mgt_sno}/review")
	public String reviewWrite(@PathVariable Integer crtfc_upso_mgt_sno, 
			                  @Valid @ModelAttribute ReviewForm reviewForm,
			                  @CurrentAccount Member member, Model model) throws Exception {
		reviewService.saveReview(reviewForm, member, crtfc_upso_mgt_sno);
		return "redirect:/api/{crtfc_upso_mgt_sno}";
	}

	@GetMapping("/reviewList")  // 커뮤니티 (리뷰 리스트)
	public String reviewList(Model model){
		List<Review> reviewList = reviewService.findAllReviews();
		model.addAttribute("reviewList",reviewList);
		return "commu";
	}
}
