package com.vietbank.specification;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import com.vietbank.entity.User;
import com.vietbank.enums.Role;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class CustomerSearchSpecification {
	
	public static Specification<User> searchCustomers(String name, String phone, String idCard, Role role) {
        return (Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // lọc theo vai trò
            predicates.add((Predicate) cb.equal(root.get("role"), role));

            // lọc theo tên
            if (name != null && !name.trim().isEmpty()) {
                String normalizedName = normalize(name.trim()); // loại bỏ dấu tiếng Việt
                predicates.add(cb.like(cb.lower(root.get("fullName")), "%" + normalizedName.toLowerCase() + "%"));
            }

            // lọc theo số điện thoại
            if (phone != null && !phone.trim().isEmpty()) {
                predicates.add( cb.like(root.get("phoneNumber"), "%" + phone.trim() + "%"));
            }

            // lọc theo idCard
            if (idCard != null && !idCard.trim().isEmpty()) {
                predicates.add(cb.like(root.get("idCard"), "%" + idCard.trim() + "%"));
            }

            return cb.and((jakarta.persistence.criteria.Predicate[]) predicates.toArray(new Predicate[0]));
        };
    }

    private static String normalize(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        // NFD: Normalization Form Decomposition
        // Tách ký tự có dấu thành ký tự gốc + dấu				
        return Normalizer.normalize(input.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "") // xóa toàn bộ dấu
                .replaceAll("[đĐ]", "d");
    }
}
