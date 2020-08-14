package com.web.curation.model;



import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Setter
@Getter
@ToString
@Table(name = "recellboard")
public class Recell {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int recellNo;
	
	@NotNull
	private String recellUser;
	@NotNull
	private String recellPrice;
	private String recellImage;
	private String recellContent;
	private Date recellDate;
	private String recellSize;
	
	}