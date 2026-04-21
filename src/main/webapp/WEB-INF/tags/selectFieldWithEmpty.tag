<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ attribute name="name" required="true" rtexprvalue="true"
              description="Name of corresponding property in bean object" %>
<%@ attribute name="label" required="true" rtexprvalue="true"
              description="Label appears in red color if input is considered as invalid after submission" %>
<%@ attribute name="names" required="true" rtexprvalue="true" type="java.util.Map"
              description="Map of values to i18n message keys" %>
<%@ attribute name="size" required="true" rtexprvalue="true"
              description="Size of Select" %>
<%@ attribute name="emptyLabel" required="false" rtexprvalue="true"
              description="Label for empty option (default: -- Select --)" %>

<spring:bind path="${name}">
    <c:set var="cssGroup" value="form-group ${status.error ? 'error' : '' }"/>
    <c:set var="valid" value="${not status.error and not empty status.actualValue}"/>
    <div class="${cssGroup}">
        <label class="col-sm-2 control-label">${label}</label>

        <div class="col-sm-10">
            <form:select class="form-control" path="${name}" size="${size}">
                <form:option value="" label="${not empty emptyLabel ? emptyLabel : '-- Select --'}"/>
                <c:forEach var="entry" items="${names}">
                    <form:option value="${entry.key}">
                        <spring:message code="${entry.value}"/>
                    </form:option>
                </c:forEach>
            </form:select>
            <c:if test="${valid}">
                <span class="fa fa-ok form-control-feedback" aria-hidden="true"></span>
            </c:if>
            <c:if test="${status.error}">
                <span class="fa fa-remove form-control-feedback" aria-hidden="true"></span>
                <span class="help-inline">${status.errorMessage}</span>
            </c:if>
        </div>
    </div>
</spring:bind>
