package com.henheang.saaspractices.ntc.service.impl;

import com.henheang.saaspractices.ntc.mapper.NtcBoardMapper;
import com.henheang.saaspractices.ntc.service.NtcBoardInVO;
import com.henheang.saaspractices.ntc.service.NtcBoardOutVO;
import com.henheang.saaspractices.ntc.service.NtcBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Notice Board ServiceImpl — same pattern as SmpBoardServiceImpl in saas-olv.
 * @Service("ntcBoardService") — Spring registers this bean by name.
 * Calls the Mapper. Add business logic here if needed.
 */
@Service("ntcBoardService")
public class NtcBoardServiceImpl implements NtcBoardService {

    @Autowired
    private NtcBoardMapper ntcBoardMapper;

    @Override
    public List<NtcBoardOutVO> selectList(NtcBoardInVO inVO) throws Exception {
        return ntcBoardMapper.selectList(inVO);
    }

    @Override
    public int selectListTotCnt(NtcBoardInVO inVO) throws Exception {
        return ntcBoardMapper.selectListTotCnt(inVO);
    }

    @Override
    public NtcBoardOutVO selectDetail(NtcBoardInVO inVO) throws Exception {
        return ntcBoardMapper.selectDetail(inVO);
    }

    @Override
    public void insert(NtcBoardInVO inVO) throws Exception {
        ntcBoardMapper.insert(inVO);
    }

    @Override
    public void update(NtcBoardInVO inVO) throws Exception {
        ntcBoardMapper.update(inVO);
    }

    @Override
    public void delete(NtcBoardInVO inVO) throws Exception {
        ntcBoardMapper.delete(inVO);
    }

    @Override
    public List<NtcBoardOutVO> selectDeletedList(NtcBoardInVO inVO) throws Exception {
        return ntcBoardMapper.selectDeletedList(inVO);
    }

    @Override
    public void restore(NtcBoardInVO inVO) throws Exception {
        ntcBoardMapper.restore(inVO);
    }
}
