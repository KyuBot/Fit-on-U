package com.web.curation.model;



import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Setter
@Getter
@ToString
public class Influencer {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private String nickname;
	
	private String profile_img;
	
}
