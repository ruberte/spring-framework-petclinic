<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="petclinic" tagdir="/WEB-INF/tags" %>

<petclinic:layout pageName="owners">
    <h2>
        <c:if test="${owner['new']}">New </c:if> Owner
    </h2>
    <form:form modelAttribute="owner" class="form-horizontal" id="add-owner-form">
        <div class="form-group has-feedback">
            <petclinic:inputField label="First Name" name="firstName"/>
            <petclinic:inputField label="Last Name" name="lastName"/>
            <petclinic:inputField label="Address" name="address"/>
            <petclinic:inputField label="City" name="city"/>
            <petclinic:inputField label="Telephone" name="telephone"/>
            <spring:bind path="birthDate">
                <c:set var="cssGroup" value="form-group ${status.error ? 'has-error' : '' }"/>
                <c:set var="valid" value="${not status.error and not empty status.actualValue}"/>
                <div class="${cssGroup}">
                    <label class="col-sm-2 control-label">Birth Date</label>
                    <div class="col-sm-10">
                        <form:input class="form-control" path="birthDate" type="date"/>
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
        </div>
        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-10">
                <c:choose>
                    <c:when test="${owner['new']}">
                        <button class="btn btn-primary" type="submit">Add Owner</button>
                    </c:when>
                    <c:otherwise>
                        <button class="btn btn-primary" type="submit">Update Owner</button>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </form:form>
</petclinic:layout>
