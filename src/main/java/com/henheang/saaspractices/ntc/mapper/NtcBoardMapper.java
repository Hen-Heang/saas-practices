package com.henheang.saaspractices.ntc.mapper;

import com.henheang.saaspractices.ntc.service.NtcBoardInVO;
import com.henheang.saaspractices.ntc.service.NtcBoardOutVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Notice Board Mapper — the same pattern as SmpBoardMapper in saas-olv.
 * @Mapper = MyBatis auto-generates the implementation.
 * Method names MUST match the id in NtcBoard_SQL.xml exactly.
 */
@Mapper
public interface NtcBoardMapper {
    List<NtcBoardOutVO> selectList(NtcBoardInVO inVO) throws Exception;
    int selectListTotCnt(NtcBoardInVO inVO) throws Exception;
    NtcBoardOutVO selectDetail(NtcBoardInVO inVO) throws Exception;
    void insert(NtcBoardInVO inVO) throws Exception;
    void update(NtcBoardInVO inVO) throws Exception;
    void delete(NtcBoardInVO inVO) throws Exception;

    List<NtcBoardOutVO> selectDeletedList(NtcBoardInVO inVO) throws Exception;
    void restore(NtcBoardInVO inVO) throws Exception;
}
