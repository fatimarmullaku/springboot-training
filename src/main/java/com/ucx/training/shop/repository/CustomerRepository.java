package com.ucx.training.shop.repository;

import com.ucx.training.shop.entity.Customer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Tuple;
import java.util.List;

@Repository
public interface CustomerRepository extends BaseRepository<Customer,Integer> {
    List<Customer> findAllByName(String name);

    @Query(value = "SELECT c.id AS id, c.name AS name, a.street AS street, a.zip_code AS zip_code, a.country AS country FROM customer c " +
            "INNER JOIN address a ON c.id = a.costumer_id " +
            "WHERE c.id = :id", nativeQuery = true)
    Tuple readCostumerById(@Param("id") Integer id);

    Customer findByEmail(String email);
}
