package org.example.capstone_3.Repository;

import org.example.capstone_3.Model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {

    Admin findAdminById(Integer id);

    Admin findAdminByEmail(String email);
}