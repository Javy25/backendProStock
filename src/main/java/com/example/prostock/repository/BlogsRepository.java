package com.example.prostock.repository;

import com.example.prostock.model.Blogs;
import com.example.prostock.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Repositorio Spring Data para consultar y guardar publicaciones del blog. */
@Repository
public interface BlogsRepository extends JpaRepository<Blogs, Long> {

}