package io.ggammu.realquerydsl.repository;

import io.ggammu.realquerydsl.dto.MemberSearchCondition;
import io.ggammu.realquerydsl.dto.MemberTeamDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomizedMemberRepository {

    List<MemberTeamDto> search(MemberSearchCondition condition);

    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);

    Page<MemberTeamDto> searchPageSimple2(MemberSearchCondition condition, Pageable pageable);

    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);


}
