package com.developmentontheedge.be5.server.services.events;

import java.lang.annotation.ElementType;
	import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogBe5Event
{
}
