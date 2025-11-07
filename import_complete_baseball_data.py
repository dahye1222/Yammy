import pandas as pd
import mysql.connector
from datetime import datetime
import os

# CSV 파일 경로
csv_dir = r"C:\Users\SSAFY\Desktop\야구"

# MySQL 연결 설정
db_config = {
    'host': 'localhost',
    'user': 'root',
    'password': 'ssafy',
    'database': 'useditem'
}

def parse_time(time_str):
    """시간 문자열을 파싱 (HH:MM 형식)"""
    if pd.isna(time_str) or time_str == '':
        return None
    try:
        return datetime.strptime(str(time_str), '%H:%M').time()
    except:
        return None

def parse_date(date_str):
    """날짜 문자열을 파싱 (YYYYMMDD 형식)"""
    if pd.isna(date_str) or date_str == '':
        return None
    try:
        return datetime.strptime(str(date_str), '%Y%m%d').date()
    except:
        return None

def import_game_info():
    """gameinfo_2025.csv 데이터를 DB에 삽입"""
    print("\n" + "="*50)
    print("game_info 테이블 데이터 임포트 시작...")
    print("="*50)

    csv_path = os.path.join(csv_dir, "gameinfo_2025.csv")
    df = pd.read_csv(csv_path, encoding='utf-8-sig')

    conn = mysql.connector.connect(**db_config)
    cursor = conn.cursor()

    # 외래키 체크 비활성화
    cursor.execute("SET FOREIGN_KEY_CHECKS=0")

    # 기존 데이터 삭제 여부
    response = input("기존 game_info 데이터를 삭제하시겠습니까? (y/n): ")
    if response.lower() == 'y':
        cursor.execute("DELETE FROM game_info")
        print("기존 데이터 삭제 완료")

    insert_query = """
        INSERT INTO game_info (
            matchcode, gwrbi, gametime, stadium, endtime, referee,
            triple, cs, sb, pickoff, starttime, passedball, err,
            oob, doublehit, doubleout, wildpitch, homerun, crowd
        ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
    """

    success_count = 0
    error_count = 0

    for index, row in df.iterrows():
        try:
            values = (
                row.get('matchcode'),
                row.get('gwrbi') if pd.notna(row.get('gwrbi')) else None,
                row.get('gametime') if pd.notna(row.get('gametime')) else None,
                row.get('stadium') if pd.notna(row.get('stadium')) else None,
                row.get('endtime') if pd.notna(row.get('endtime')) else None,
                row.get('referee') if pd.notna(row.get('referee')) else None,
                row.get('triple') if pd.notna(row.get('triple')) else None,
                row.get('cs') if pd.notna(row.get('cs')) else None,
                row.get('sb') if pd.notna(row.get('sb')) else None,
                row.get('pickoff') if pd.notna(row.get('pickoff')) else None,
                row.get('starttime') if pd.notna(row.get('starttime')) else None,
                row.get('passedball') if pd.notna(row.get('passedball')) else None,
                row.get('err') if pd.notna(row.get('err')) else None,
                row.get('oob') if pd.notna(row.get('oob')) else None,
                row.get('doublehit') if pd.notna(row.get('doublehit')) else None,
                row.get('doubleout') if pd.notna(row.get('doubleout')) else None,
                row.get('wildpitch') if pd.notna(row.get('wildpitch')) else None,
                row.get('homerun') if pd.notna(row.get('homerun')) else None,
                row.get('crowd') if pd.notna(row.get('crowd')) else None
            )

            cursor.execute(insert_query, values)
            success_count += 1

            if (index + 1) % 100 == 0:
                print(f"{index + 1}개 레코드 처리 중...")

        except Exception as e:
            error_count += 1
            print(f"행 {index} 처리 중 오류: {e}")

    cursor.execute("SET FOREIGN_KEY_CHECKS=1")
    conn.commit()
    cursor.close()
    conn.close()

    print(f"\n완료! 성공: {success_count}개, 실패: {error_count}개")

def import_match_schedule():
    """gameschedule_2025.csv 데이터를 DB에 삽입"""
    print("\n" + "="*50)
    print("match_schedule 테이블 데이터 임포트 시작...")
    print("="*50)

    csv_path = os.path.join(csv_dir, "gameschedule_2025.csv")
    df = pd.read_csv(csv_path, encoding='utf-8-sig')

    conn = mysql.connector.connect(**db_config)
    cursor = conn.cursor()

    # 외래키 체크 비활성화
    cursor.execute("SET FOREIGN_KEY_CHECKS=0")

    # 기존 데이터 삭제 여부
    response = input("기존 match_schedule 데이터를 삭제하시겠습니까? (y/n): ")
    if response.lower() == 'y':
        cursor.execute("DELETE FROM match_schedule")
        print("기존 데이터 삭제 완료")

    insert_query = """
        INSERT INTO match_schedule (
            match_status, match_date, home, away, dbheader, gameid, year
        ) VALUES (%s, %s, %s, %s, %s, %s, %s)
    """

    success_count = 0
    error_count = 0

    for index, row in df.iterrows():
        try:
            values = (
                row.get('match_status') if pd.notna(row.get('match_status')) else None,
                str(row.get('match_date')) if pd.notna(row.get('match_date')) else None,
                row.get('home') if pd.notna(row.get('home')) else None,
                row.get('away') if pd.notna(row.get('away')) else None,
                int(row.get('dbheader')) if pd.notna(row.get('dbheader')) else None,
                row.get('gameid') if pd.notna(row.get('gameid')) else None,
                int(row.get('year')) if pd.notna(row.get('year')) else None
            )

            cursor.execute(insert_query, values)
            success_count += 1

            if (index + 1) % 100 == 0:
                print(f"{index + 1}개 레코드 처리 중...")

        except Exception as e:
            error_count += 1
            print(f"행 {index} 처리 중 오류: {e}")

    cursor.execute("SET FOREIGN_KEY_CHECKS=1")
    conn.commit()
    cursor.close()
    conn.close()

    print(f"\n완료! 성공: {success_count}개, 실패: {error_count}개")

def import_scoreboard():
    """scoreboard_2025.csv 데이터를 DB에 삽입"""
    print("\n" + "="*50)
    print("scoreboard 테이블 데이터 임포트 시작...")
    print("="*50)

    csv_path = os.path.join(csv_dir, "scoreboard_2025.csv")
    df = pd.read_csv(csv_path, encoding='utf-8-sig')

    conn = mysql.connector.connect(**db_config)
    cursor = conn.cursor()

    # 외래키 체크 비활성화
    cursor.execute("SET FOREIGN_KEY_CHECKS=0")

    # 기존 데이터 삭제 여부 확인
    response = input("기존 scoreboard 데이터를 삭제하시겠습니까? (y/n): ")
    if response.lower() == 'y':
        cursor.execute("DELETE FROM scoreboard")
        print("기존 데이터 삭제 완료")

    insert_query = """
        INSERT INTO scoreboard (
            matchcode, idx, team, result,
            i_1, i_2, i_3, i_4, i_5, i_6, i_7, i_8, i_9,
            i_10, i_11, i_12, i_13, i_14, i_15, i_16, i_17, i_18,
            run, hit, err, balls,
            matchdate, matchday, home, away, dbheader, place, audience,
            starttime, endtime, gametime
        ) VALUES (
            %s, %s, %s, %s,
            %s, %s, %s, %s, %s, %s, %s, %s, %s,
            %s, %s, %s, %s, %s, %s, %s, %s, %s,
            %s, %s, %s, %s,
            %s, %s, %s, %s, %s, %s, %s,
            %s, %s, %s
        )
    """

    success_count = 0
    error_count = 0

    for index, row in df.iterrows():
        try:
            # matchdate는 matchday(YYYYMMDD)에서 추출
            matchdate = parse_date(row.get('matchday'))

            values = (
                row['matchcode'],
                int(row['idx']) if pd.notna(row.get('idx')) else None,
                row.get('team'),
                str(row.get('result')) if pd.notna(row.get('result')) else None,
                # 이닝별 득점
                int(row.get('i_1')) if pd.notna(row.get('i_1')) else None,
                int(row.get('i_2')) if pd.notna(row.get('i_2')) else None,
                int(row.get('i_3')) if pd.notna(row.get('i_3')) else None,
                int(row.get('i_4')) if pd.notna(row.get('i_4')) else None,
                int(row.get('i_5')) if pd.notna(row.get('i_5')) else None,
                int(row.get('i_6')) if pd.notna(row.get('i_6')) else None,
                int(row.get('i_7')) if pd.notna(row.get('i_7')) else None,
                int(row.get('i_8')) if pd.notna(row.get('i_8')) else None,
                int(row.get('i_9')) if pd.notna(row.get('i_9')) else None,
                int(row.get('i_10')) if pd.notna(row.get('i_10')) else None,
                int(row.get('i_11')) if pd.notna(row.get('i_11')) else None,
                int(row.get('i_12')) if pd.notna(row.get('i_12')) else None,
                int(row.get('i_13')) if pd.notna(row.get('i_13')) else None,
                int(row.get('i_14')) if pd.notna(row.get('i_14')) else None,
                int(row.get('i_15')) if pd.notna(row.get('i_15')) else None,
                int(row.get('i_16')) if pd.notna(row.get('i_16')) else None,
                int(row.get('i_17')) if pd.notna(row.get('i_17')) else None,
                int(row.get('i_18')) if pd.notna(row.get('i_18')) else None,
                # 총합
                int(row.get('run')) if pd.notna(row.get('run')) else None,
                int(row.get('hit')) if pd.notna(row.get('hit')) else None,
                int(row.get('err')) if pd.notna(row.get('err')) else None,
                int(row.get('balls')) if pd.notna(row.get('balls')) else None,
                # 경기 정보
                matchdate,
                str(row.get('matchday')) if pd.notna(row.get('matchday')) else None,
                row.get('home'),
                row.get('away'),
                row.get('dbheader'),
                row.get('place'),
                int(row.get('audience')) if pd.notna(row.get('audience')) else None,
                parse_time(row.get('starttime')),
                parse_time(row.get('endtime')),
                row.get('gametime')
            )

            cursor.execute(insert_query, values)
            success_count += 1

            if (index + 1) % 100 == 0:
                print(f"{index + 1}개 레코드 처리 중...")

        except Exception as e:
            error_count += 1
            print(f"행 {index} 처리 중 오류: {e}")

    cursor.execute("SET FOREIGN_KEY_CHECKS=1")
    conn.commit()
    cursor.close()
    conn.close()

    print(f"\n완료! 성공: {success_count}개, 실패: {error_count}개")

if __name__ == "__main__":
    print("=" * 50)
    print("야구 데이터 전체 임포트 스크립트")
    print("=" * 50)
    print()
    print("현재 DB 설정:")
    print(f"  Host: {db_config['host']}")
    print(f"  User: {db_config['user']}")
    print(f"  Database: {db_config['database']}")
    print()

    response = input("DB 설정이 맞습니까? 계속하려면 'y'를 입력하세요: ")
    if response.lower() != 'y':
        print("취소되었습니다.")
        exit()

    # 1. game_info 임포트
    import_game_info()

    # 2. match_schedule 임포트
    import_match_schedule()

    # 3. scoreboard 임포트
    import_scoreboard()

    print("\n" + "=" * 50)
    print("모든 데이터 임포트 완료!")
    print("=" * 50)
