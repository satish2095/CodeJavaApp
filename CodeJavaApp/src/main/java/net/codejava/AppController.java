package net.codejava;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

@Controller
public class AppController {

	@Autowired
	private UserRepository userRepo;

	@GetMapping
	public String viewHomePage() {
		return "index";
	}

	@GetMapping("/register")
	public String showRegistrationForm(Model model) {
		model.addAttribute("user", new User());
		return "signup_form";
	}

	@PostMapping("/process_register")
	public String processRegister(User user) {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encodedPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodedPassword);
		userRepo.save(user);
		return "register_success";
	}

	@GetMapping("/list_users")
	public String listUsers(Model model) {
		List<User> listUsers = userRepo.findAll();
		model.addAttribute("listUsers", listUsers);
		return "users";
	}

	@GetMapping("/users")
	public String userHomePage(Model model, Principal principal) {
		try {
			String userId = principal.getName();
			User user = userRepo.findPhoneNumberByEmail(userId);
			System.out.println(user);
			String otp = String.valueOf(new Random().nextInt(999999));
			user.setOtp(otp);
			userRepo.saveAndFlush(user);
			user.setOtp("");
			model.addAttribute("user", user);
			RestTemplate restTemplate = new RestTemplate();

			String url = "https://www.fast2sms.com/dev/bulk";
			String body = "sender_id=FSTSMS&message=Your otp is " + otp + "&language=english&route=p&numbers="
					+ user.getPhonenumber();

			HttpHeaders headers = new HttpHeaders();
			headers.add("authorization",
					"VlAKSdORfEt0juxTvnJyIDZLF57NUz81iCcwG3sm2YPqBehaob4YiSZMjORd5nlmFU1GL3o8wxWBDasT");
			headers.add("Content-Type", "application/x-www-form-urlencoded");

			HttpEntity<String> request = new HttpEntity<>(body, headers);

			ResponseEntity<String> postForEntity = restTemplate.postForEntity(url, request, String.class);
			System.out.println(postForEntity.getStatusCodeValue());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "otp";

	}

	@PostMapping("/verifyOtp")
	public String verifyOtp(Model model, User user) {
		User saveuser = userRepo.findOtpByEmail(user.getEmail());
		if (saveuser.getOtp().equalsIgnoreCase(user.getOtp())) {
			saveuser.setOtp("");
			userRepo.save(saveuser);
			List<User> listUsers = userRepo.findAll();
			model.addAttribute("listUsers", listUsers);
			return "users";
		} else {
			return "invalidotp";
		}
	}

}