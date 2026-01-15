import http from 'k6/http';
import { check, sleep } from 'k6';
import { group } from 'k6';

export const options = {
    stages: [
        { duration: '1m', target: 20 },  // 1분 동안 50명까지 증가
        { duration: '3m', target: 20 },  // 3분 동안 유지
        { duration: '1m', target: 0 },   // 종료
    ],  thresholds: {
        'http_req_duration{name:es}': ['p(95)<500'], // 메인 API의 95% 응답은 500ms 미만일 것
        'http_req_duration{name:rdb}': ['p(95)<500'],
    },
};

const BASE_URL = 'http://localhost:8080';


export default function () {

    const headers = {
        'Content-Type': 'application/json',
    };

    // API 테스트 루프
    group('ES API', function () {
        let res = http.get(`${BASE_URL}/api/v1/tour-post/search?language=KO&sort=DISTANCE&mapX=127.008899&mapY=37.565810&title=true&keyword=%EB%AC%B8`, {headers,  tags: { name: 'es' } });
        check(res, {'ES Search status is 200': (r) => r.status === 200});
    })

    group('JOOQ API', function () {
        let res = http.get(`${BASE_URL}/api/v1/tour-post/searchDsl?language=KO&sort=DISTANCE&mapX=127.008899&mapY=37.565810&title=true&keyword=%EB%AC%B8`, {headers, tags: { name: 'rdb' } });
        check(res, {'JOOQ Search status is 200': (r) => r.status === 200});
    })



}