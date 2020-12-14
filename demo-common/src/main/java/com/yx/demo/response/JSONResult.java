package com.yx.demo.response;

import com.alibaba.fastjson.JSON;
import com.yx.demo.enums.ExceptionEnum;
import org.springframework.context.MessageSource;

import java.util.Locale;

public class JSONResult<T>
{

	private boolean success = true;
	
	private String code;
	
	private String message;
	
	private T data;

	public boolean isSuccess()
	{
		return success;
	}

	public void setSuccess(T data)
	{
		this.data = data;
		this.code = "200";
		this.message = "成功！";
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getData()
	{
		return data;
	}

	public void setData(T data)
	{
		this.data = data;
	}

	public String toJson()
	{
			return JSON.toJSONString(this);
	}

	public void setError(String errorCode, MessageSource messageSource,
			Object... params)
	{
		this.setSuccess(false);
		this.setCode(errorCode);
		this.setMessage(messageSource.getMessage(errorCode, params,
				Locale.SIMPLIFIED_CHINESE));
	}

	private void setSuccess(boolean success) {
		this.success = success;
	}

	@SuppressWarnings("all")
	public void setError(String errorCode, MessageSource messageSource)
	{
		setError(errorCode, messageSource, null);
	}

	public void setError(String errorCode,String message)
	{
		this.setSuccess(false);
		this.setCode(errorCode);
		this.setMessage(message);
	}

	public void setError(ExceptionEnum e){
		this.code = e.getCode();
		this.message = e.getMessage();
	}
}
