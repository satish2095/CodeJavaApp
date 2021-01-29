package net.codejava;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	User findByEmail(String email);
	
	User findPhoneNumberByEmail(String email);
	
	User findOtpByEmail(String email);

}
