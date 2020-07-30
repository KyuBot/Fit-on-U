package com.web.curation.model;

import java.time.LocalDateTime;

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
public class Board {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // 기본키 매핑 db에서 자동증분으로 설정
	private int articleNo;
	
	// 컬럼 어노테이션 없으면 컬럼명의 디폴트가 필드명이 됨
	private String content;
	
	@Column(insertable = false, updatable = false) // 일기전용시 false
	private LocalDateTime articleDate;
	 
	private String articleUser;
	private String influeUser;
	
	private int favoriteCnt;
}
