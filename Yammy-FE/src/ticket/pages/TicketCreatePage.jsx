import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getTeamColors } from '../../sns/utils/teamColors';
import { createTicket } from '../api/ticketApi';
import '../styles/TicketCreatePage.css';

const TicketCreatePage = () => {
    const navigate = useNavigate();
    const teamColors = getTeamColors();
    const [currentStep, setCurrentStep] = useState(1);
    const [formData, setFormData] = useState({
        game: '',
        date: '',
        location: '',
        seat: '',
        comment: '',
        type: '',
        awayScore: '',
        homeScore: '',
        review: '',
        photo: null,
        photoPreview: null,
    });
    const [showLocationModal, setShowLocationModal] = useState(false);

    // 경기장 목록
    const stadiums = [
        '강릉종합운동장',
        '강화SSG퓨처스필드',
        '경남e스포츠상설경기장',
        '경민대학교 기념관',
        '경주축구공원',
        '계양체육관',
        '고양국기대표아구훈련장',
        '고양소노아레나',
        '고척스카이돔',
        '광양축구전용구장',
        '광주-기아챔피언스필드'
    ];

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handlePhotoChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onloadend = () => {
                setFormData(prev => ({
                    ...prev,
                    photo: file,
                    photoPreview: reader.result
                }));
            };
            reader.readAsDataURL(file);
        }
    };

    const handleLocationSelect = (stadium) => {
        setFormData(prev => ({
            ...prev,
            location: stadium
        }));
        setShowLocationModal(false);
    };

    const nextStep = () => {
        if (currentStep === 1) {
            if (!formData.game || !formData.date || !formData.location || !formData.seat || !formData.comment) {
                alert('모든 필수 항목을 입력해주세요.');
                return;
            }
        }
        if (currentStep === 2) {
            if (!formData.photoPreview) {
                alert('사진을 선택해주세요.');
                return;
            }
        }
        if (currentStep < 3) {
            setCurrentStep(currentStep + 1);
        }
    };

    const prevStep = () => {
        if (currentStep > 1) {
            setCurrentStep(currentStep - 1);
        }
    };

    const handleSubmit = async () => {
        try {
            await createTicket(formData);
            alert('티켓이 발급되었습니다!');
            navigate('/ticket/list');
        } catch (error) {
            console.error('티켓 발급 실패:', error);
            alert('티켓 발급에 실패했습니다.');
        }
    };

    return (
        <div
            className="ticket-create-page"
            style={{
                '--team-color': teamColors.bgColor,
                '--team-text-color': teamColors.textColor
            }}
        >
            {/* 헤더 */}
            <div className="ticket-header" style={{ backgroundColor: teamColors.bgColor }}>
                <button onClick={() => navigate(-1)} className="back-btn" style={{ color: teamColors.textColor }}>
                    ←
                </button>
                <h1 className="header-title" style={{ color: teamColors.textColor }}>티켓 발급</h1>
                <div style={{ width: '40px' }}></div>
            </div>

            {/* 진행 단계 */}
            <div className="progress-steps">
                <div className={`step ${currentStep >= 1 ? 'active' : ''}`}>
                    <div className="step-circle">✓</div>
                    <div className="step-label">경기결과</div>
                </div>
                <div className="step-line"></div>
                <div className={`step ${currentStep >= 2 ? 'active' : ''}`}>
                    <div className="step-circle">✗</div>
                    <div className="step-label">사진선택</div>
                </div>
                <div className="step-line"></div>
                <div className={`step ${currentStep >= 3 ? 'active' : ''}`}>
                    <div className="step-circle">✗</div>
                    <div className="step-label">필수정보</div>
                </div>
            </div>

            {/* 단계별 폼 */}
            <div className="ticket-form-container">
                {/* 1단계: 경기결과 */}
                {currentStep === 1 && (
                    <div className="form-step">
                        <div className="form-group">
                            <label>Game*</label>
                            <input
                                type="text"
                                name="game"
                                value={formData.game}
                                onChange={handleChange}
                                placeholder="예시: 대한민국 vs 우리나라"
                            />
                        </div>

                        <div className="form-group">
                            <label>Date*</label>
                            <input
                                type="date"
                                name="date"
                                value={formData.date}
                                onChange={handleChange}
                            />
                        </div>

                        <div className="form-group">
                            <label>Location*</label>
                            <input
                                type="text"
                                name="location"
                                value={formData.location}
                                onClick={() => setShowLocationModal(true)}
                                placeholder="경기장을 선택해주세요"
                                readOnly
                            />
                        </div>

                        <div className="form-group">
                            <label>Seat*</label>
                            <input
                                type="text"
                                name="seat"
                                value={formData.seat}
                                onChange={handleChange}
                                placeholder="예시: A구역 4행 1열"
                            />
                        </div>

                        <div className="form-group">
                            <label>Comment*</label>
                            <input
                                type="text"
                                name="comment"
                                value={formData.comment}
                                onChange={handleChange}
                                placeholder="직관한 경기 한줄평을 남겨주세요."
                            />
                        </div>

                        <button className="next-btn" onClick={nextStep} style={{ backgroundColor: teamColors.bgColor }}>
                            다음
                        </button>
                    </div>
                )}

                {/* 2단계: 사진선택 */}
                {currentStep === 2 && (
                    <div className="form-step">
                        <div className="photo-upload-section">
                            <div className="photo-preview">
                                {formData.photoPreview ? (
                                    <img src={formData.photoPreview} alt="미리보기" />
                                ) : (
                                    <div className="photo-placeholder">
                                        <div className="photo-icon">📷</div>
                                        <p>사진 선택</p>
                                    </div>
                                )}
                            </div>
                            <p className="photo-guide">필수값(*)을 채워 관람한 경기를 티켓으로 완성하세요.</p>
                            <input
                                type="file"
                                accept="image/*"
                                onChange={handlePhotoChange}
                                style={{ display: 'none' }}
                                id="photo-input"
                            />
                            <label htmlFor="photo-input" className="photo-upload-btn" style={{ backgroundColor: teamColors.bgColor }}>
                                사진 선택
                            </label>
                        </div>

                        <div className="form-buttons">
                            <button className="prev-btn" onClick={prevStep}>이전</button>
                            <button className="next-btn" onClick={nextStep} style={{ backgroundColor: teamColors.bgColor }}>다음</button>
                        </div>
                    </div>
                )}

                {/* 3단계: 필수정보 */}
                {currentStep === 3 && (
                    <div className="form-step">
                        <div className="form-group">
                            <label>Type</label>
                            <input
                                type="text"
                                name="type"
                                value={formData.type}
                                onChange={handleChange}
                                placeholder="관람한 종목을 선택해주세요."
                            />
                        </div>

                        <div className="form-group">
                            <label>Score</label>
                            <div className="score-inputs">
                                <input
                                    type="number"
                                    name="awayScore"
                                    value={formData.awayScore}
                                    onChange={handleChange}
                                    placeholder="Away"
                                />
                                <span>:</span>
                                <input
                                    type="number"
                                    name="homeScore"
                                    value={formData.homeScore}
                                    onChange={handleChange}
                                    placeholder="Home"
                                />
                            </div>
                        </div>

                        <div className="form-group">
                            <label>Review</label>
                            <textarea
                                name="review"
                                value={formData.review}
                                onChange={handleChange}
                                rows={6}
                                placeholder="상세 리뷰를 작성해주세요..."
                            />
                        </div>

                        <div className="form-buttons">
                            <button className="prev-btn" onClick={prevStep}>이전</button>
                            <button className="submit-btn" onClick={handleSubmit} style={{ backgroundColor: teamColors.bgColor }}>
                                티켓 발급하기
                            </button>
                        </div>
                    </div>
                )}
            </div>

            {/* 경기장 선택 모달 */}
            {showLocationModal && (
                <div className="location-modal" onClick={() => setShowLocationModal(false)}>
                    <div className="location-modal-content" onClick={(e) => e.stopPropagation()}>
                        <button className="modal-close" onClick={() => setShowLocationModal(false)}>✕</button>
                        <input
                            type="text"
                            placeholder="경기장 이름 검색"
                            className="location-search"
                        />
                        <div className="stadium-list">
                            {stadiums.map(stadium => (
                                <div
                                    key={stadium}
                                    className="stadium-item"
                                    onClick={() => handleLocationSelect(stadium)}
                                >
                                    <span className="location-icon">📍</span>
                                    {stadium}
                                </div>
                            ))}
                        </div>
                        <button
                            className="direct-input-btn"
                            onClick={() => {
                                const custom = prompt('경기장 이름을 직접 입력하세요:');
                                if (custom) handleLocationSelect(custom);
                            }}
                        >
                            직접 입력
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default TicketCreatePage;
