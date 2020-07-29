package com.web.curation.controller.account;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.StringTokenizer;

import javax.mail.internet.MimeMessage;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.io.Files;
import com.web.curation.dao.user.UserDao;
import com.web.curation.model.BasicResponse;
import com.web.curation.model.UserDTO;
import com.web.curation.model.user.SignupRequest;
import com.web.curation.model.user.User;
import com.web.curation.service.user.JwtService;
import com.web.curation.service.user.UserLoginService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javassist.NotFoundException;

@ApiResponses(value = { @ApiResponse(code = 401, message = "Unauthorized", response = BasicResponse.class),
		@ApiResponse(code = 403, message = "Forbidden", response = BasicResponse.class),
		@ApiResponse(code = 404, message = "Not Found", response = BasicResponse.class),
		@ApiResponse(code = 500, message = "Failure", response = BasicResponse.class) })

@CrossOrigin(origins = { "*" })
@RestController
public class AccountController {

	@Autowired
	private JavaMailSender emailSender;

	@Autowired
	UserDao userDao;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UserLoginService userService;

	@GetMapping("/account/login")
	@ApiOperation(value = "로그인")
	public Object login(@RequestParam(required = true) final String email,
			@RequestParam(required = true) final String password) {

//      Optional<User> userOpt = userDao.findUserByEmailAndPassword(email, password);

		ResponseEntity response = null;

		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = HttpStatus.ACCEPTED;
		try {
			UserDTO loginUser = userService.login(email, password);
			System.out.println(loginUser.getEmail());
			System.out.println(loginUser.getPassword());

			// 아이디가 존재하지 않으면 result -1
			if (loginUser.getEmail() == null || !loginUser.getEmail().equals(email)) {
				resultMap.put("result", -1);
			}

			// 비밀번호가 틀리다면 res 2
			else if (!loginUser.getPassword().equals(password)) {
				resultMap.put("result", 2);
			}
			// 로그인 성공 했다면 토큰 생성 후 result 1
			// 토큰 정보는 request의 헤더로 보내고 나머지는 Map에 답는다.
			else {
				String token = jwtService.create(loginUser);
				System.out.println(token);
				resultMap.put("auth_token", token);
				resultMap.put("result", 1);
				resultMap.put("data", loginUser);
			}

			// res.setHeader("jwt-auth-token",token);
		} catch (NotFoundException e) {
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			e.printStackTrace();
		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}

	@PostMapping("/account/signup")
	@ApiOperation(value = "가입하기")
	public Object signup(@Valid @RequestBody SignupRequest request) {
		Map<String, Object> resultMap = new HashMap<>();
	      // 이메일, 닉네임 중복처리 필수
	      // 회원가입단을 생성해 보세요.
		System.out.println(1);
		System.out.println(request.getProfile_img());
	      final BasicResponse result = new BasicResponse();
	      // 이메일, 닉네임 중복처리 필수

	      // 저장
	      User user = new User();
	      StringTokenizer st = new StringTokenizer(request.getBirth());
	      LocalDate currentDate = LocalDate.of(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
	      
	      System.out.println(request.getNickname());
	      user.setNickname(request.getNickname());
	      user.setEmail(request.getEmail());
	      user.setBirth(currentDate);
	      user.setGender(request.getGender());
	      user.setPassword(request.getPassword());
	      user.setSelfintroduce(null);
	      user.setProfile_img(request.getProfile_img());
	      if (userDao.findUserByNickname(user.getNickname()).isPresent()
	            || userDao.findUserByEmail(user.getEmail()).isPresent()) {
	         result.status = true;
	         result.data = "fail";
	      } else {
	         if (userDao.save(user) == null) {
	            result.status = true;
	            result.data = "fail";

	         } else {
	        	UserDTO userDTO = new UserDTO(user);
	        	String Token = jwtService.create(userDTO);
	        	
	            result.status = true;
	            result.data = "success";
	            resultMap.put("result", result);
	            resultMap.put("auth_token",Token);
	         }
	      }
	      return new ResponseEntity<>(resultMap, HttpStatus.OK);
	   }

		

	@GetMapping("/account/findPassword")
	@ApiOperation(value = "비밀번호 찾기")
	public Map<String, Object> findPassword(@Valid @RequestParam String email, @Valid @RequestParam String pTime) {
		Map<String, Object> result = new HashMap<>();
		LocalDate time = LocalDate.of(Integer.parseInt(pTime.substring(0, 4)), Integer.parseInt(pTime.substring(4, 6)),
				Integer.parseInt(pTime.substring(6, 8)));
		Optional<User> optUser = userDao.findUserByEmailAndBirth(email, time);
		if (!optUser.isPresent()) {
		} else {
			UserDTO userDto = new UserDTO(optUser.get());

			String to = userDto.getEmail();
			String subject = "핏온유 비밀번호 인증입니다 확인해 주세요";
			int randomCode = new Random().nextInt(9000) + 1000;
			String certificationNum = Integer.toString(randomCode);
			StringBuilder text = new StringBuilder();
			text.append(userDto.getNickname());
			text.append(" 님의 비밀번호를 위한 비밀번호 인증 절차입니다 하단의 번호를 핏온유 화면에 입력해 주세요\n");
			text.append("인증번호:" + certificationNum + '\n');
			text.append("인증번호를 다른사람이 보지 않게 주의해 주세요.\n");
//			text.append("핏온유 인증 페이지로 이동하기");
//			text.append("http://localhost:8081/");

			MimeMessage message = emailSender.createMimeMessage();
			try {
				System.out.println(4);
				MimeMessageHelper helper = new MimeMessageHelper(message, true);
				helper.setFrom("ouosssssssa@gmail.com");
				helper.setTo(to);
				helper.setSubject(subject);
				helper.setText(text.toString());
				emailSender.send(message);
				result.put("userInfo", userDto);
				result.put("certifNum", certificationNum);

			} catch (Exception e) {
				System.out.println(5);
				e.printStackTrace();
			}
		}
		return result;
	}

	@PostMapping(value = "/account/addProfileImg", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	@ApiOperation(value = "가입하기")

	public Object addProfileImg(@RequestParam("profile-img-edit") MultipartFile img) {

		final BasicResponse result = new BasicResponse();
		String path = "C:\\Users\\multicampus\\Desktop\\picture\\";
		File file = new File(path + img.getOriginalFilename());
		try {
			img.transferTo(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(img);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/account/checkDoubleEmail")
	@ApiOperation(value = "이메일 중복검사")
	public Object findEmail(@Valid @RequestParam String email) {
		final BasicResponse result = new BasicResponse();

		Optional<User> optUser = userDao.getUserByEmail(email);
		if (!optUser.isPresent()) {// 없는 경우
			result.status = true;
			result.data = "non exist";
		} else {// 있는 경우
			result.status = true;
			result.data = "exist";
			result.object = optUser.get();
		}
		return result;
	}

	@GetMapping("/account/checkNickname")
	@ApiOperation(value = "닉네임 중복검사")
	public Object findNick(@Valid @RequestParam String nickname) {
		
		final BasicResponse result = new BasicResponse();

		Optional<User> optUser = userDao.findUserByNickname(nickname);
		if (!optUser.isPresent()) {// 없는 경우
			result.status = true;
			result.data = "non exist";
		} else {// 있는 경우
			result.status = true;
			result.data = "exist"; 
			result.object = optUser.get(); 
		}
		return result;
	}
	
	@PostMapping("/account/changePassword")
	@ApiOperation(value = "새 비밀번호 설정")
	public Object changePwd(@Valid @RequestParam("email") String email,@Valid @RequestParam("password") String password) {
		Map<String, Object> resultMap = new HashMap<>();
		
		System.out.println(email+ " "+password);
		final BasicResponse result = new BasicResponse();
		try{
			userDao.updatePassword(password, email);
			result.status=true;
			result.data="success";
			UserDTO userDTO = new UserDTO(userDao.findUserByEmail(email).get());
			String Token = jwtService.create(userDTO);
			resultMap.put("auth_token",Token);
			
		}
		catch (Exception e){
			result.status=true;
			result.data="fail";
			resultMap.put("result",result);
		}
		
		return resultMap;
	}
	// user 정보 받기
	@GetMapping("/account/token")
	   public Map<String, Object> getUserByToken(@RequestParam String jwt){
	      Map<String, Object> resultMap = new HashMap<>();
	      try {
	         jwtService.checkValid(jwt); // 토큰이 유효한지 검사
	         resultMap.put("userInfo",jwtService.get(jwt)); // 토큰에 담긴 정보 담기
	         resultMap.put("result",1);
	         
	      }catch(Exception e){
	         resultMap.put("result",0);
	      }
	      return resultMap;
	   }
	
}
