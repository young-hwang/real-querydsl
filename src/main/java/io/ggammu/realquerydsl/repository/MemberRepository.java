package io.ggammu.realquerydsl.repository;

import io.ggammu.realquerydsl.entity.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long>, CustomizedMemberRepository {

    List<Member> findByUsername(String username);

}
