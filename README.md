# 멋쟁이사자처럼 백엔드 스쿨 (3기)
# 😎 `KLover Crew`

## 👨‍👨‍👧‍👦 팀원 소개

| <img src='https://github.com/sky0427.png' width=120> | <img src='https://github.com/suuny0321.png' width=120> | <img src='https://github.com/Asthux.png' width=120> | <img src='https://github.com/koothemaverick.png' width=120> | <img src='https://github.com/LSH-kw0315.png' width=120> |
| :-: | :-: | :-: | :-: | :-: |
| 윤수근 | 주선경 | 오상민 | 구준휘 | 이승헌 |
| [GitHub](https://github.com/sky0427) | [GitHub](https://github.com/suuny0321) | [GitHub](https://github.com/Asthux) | [GitHub](https://github.com/koothemaverick) | [GitHub](https://github.com/LSH-kw0315) | 

<br>
<br>

## 📌 KLover 프로젝트 개요

### 😎 KLover 서비스 소개

한국을 방문하는 외국인 관광객들이 겪는 언어 장벽,
정보 부족 등의 문제를 해결하고,
한국의 다채로운 매력을 쉽고 편리하게 경험할 수 있도록 돕는
`올인원(All-in-one) 인바운드 관광 플랫폼`

### 문제인식

작년 4월까지 우리나라에 입국한 외국인은 약 274만명으로 지난해 같은 기간에 비해 6배가량 증가했는데요.
다만 한국 재방문율은 50% 수준으로 관광대국들에 한참 못 미친다고 합니다.
처음 한국을 방문하는 외국인 관광객 수는 매년 늘고 있는데 재방문율이 높아지지 않는 것은 그럴만한 이유가 있기 때문이라고 하는데요.
어떤 부분이 외국인들이 한국에 또 오는 걸 주저하게 만들까요?

<br>

#### 1. 길 찾는 게 어려워요

구글 맵은 전 세계적으로 유용하지만, 한국에서는 지도 데이터 반출 문제와 정부의 이견 으로 인해 서비스가 매우
제한적이며, 이는 특히 외국인 관광객에게 큰 불편을 초래하고 있다.

<br>

#### 2. 민망한 엉터리 영어

한국을 방문하는 외국인 관광객이 늘고 있지만, 공공장소 및 식당 등에서 제공되는 영어 번역이 매우 부실하여
외국인 관광객에게 큰 불편을 초래하고, 한국에 대한 부정적인 인상을 심어줄 수 있다. 특히 한식에 대한 높은
관심에도 불구하고, 한식당의 번역 문제는 더욱 심각한 상황이다.

<br>

#### 3. 매력적인 관광지, 관광 코스 없어

한국이 외국인 관광객 유치를 위한 야심 찬 목표를 세웠지만, 매력적인 관광 콘텐츠 부족, 숙박 및 교통 시설 미비,
기타 불친절 문제 등 여러 요인으로 인해 재방문율이 낮다. 관광 매력 국가로 발돋움하기 위한 전반적인 개선이
필요한 상황이다.


<br>
<br>

## 🎥 시연 영상

> 아래 이미지를 클릭하면 시연 영상을 시청할 수 있습니다.


<br>
<br>

## ⚙️ 서비스 아키텍처

> 아키텍처 사진 들어갈 것것
<div>
  <!-- Spring Boot -->
  <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat-square&logo=SpringBoot&logoColor=white"/>
  <!-- Spring Security -->
  <img src="https://img.shields.io/badge/Spring_Security-339933?style=flat-square&logo=SpringSecurity&logoColor=white"/>

</div>

<div>
  <!-- React Native -->
  <img src="https://img.shields.io/badge/React Native-65ADF1?style=flat-square&logo=React&logoColor=white"/>
  <!-- TypeScript -->
  <img src="https://img.shields.io/badge/TypeScript-3178C6?style=flat-square&logo=TypeScript&logoColor=white"/>
  <!-- Redux -->
  <img src="https://img.shields.io/badge/Redux-764ABC?style=flat-square&logo=Redux&logoColor=black"/>
</div>

<div>
  <!-- MySQL -->
  <img src="https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=PostgreSQL&logoColor=white"/>
  <!-- Redis -->
  <img src="https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=Redis&logoColor=white"/>
  <!-- Redis -->
  <img src="https://img.shields.io/badge/Elastic Search-005571?style=flat-square&logo=Redis&logoColor=white"/>
  
</div>

<div>
  <!-- GitHub -->
  <img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=GitHub&logoColor=white"/>
  <!-- Docker -->
  <img src="https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=Docker&logoColor=white"/>
</div>

<div>
  <!-- AWS -->
  <img src="https://img.shields.io/badge/Amazon EC2-FF9900?style=flat-square&logo=Amazon EC2&logoColor=white"/>
  <img src="https://img.shields.io/badge/Amazon S3-569A31?style=flat-square&logo=Amazon S3&logoColor=white"/>
  <!-- GitHub Actions -->
  <img src="https://img.shields.io/badge/GitHub Actions-2088FF?style=flat-square&logo=GitHub Actions&logoColor=white"/>
  <!-- Terraform -->
  <img src="https://img.shields.io/badge/Terraform-844FBA?style=flat-square&logo=Terraform&logoColor=white"/>
</div>

<br>
<br>

## 👨‍⚖️ 기술적 의사결정

<details>
  <summary><b>[이미지 스토리지] AWS S3</b></summary>
  <div markdown="1">
    </br>
    <ul>
      <li>도입배경</li>
        <ul>
          <li>유저수, 서비스 사용량 증가 등에 따라 저장하는 사진 파일이 많아지면 스토리지 공간을 확장해야 한다. 그렇기 때문에 서비스의 확장성을 고려해 스토리지 공간이 필요한 만큼 동적으로 할당해 효율적으로 사진을 관리할 수 있는 클라우드 기반의 객체 스토리지 서비스가 필요하였다.</li>
        </ul>
      </br>
      <li>기술비교</li>
        <table width="70%">
          <thead>
            <tr>
              <th></th>
              <th>AWS S3</th>
              <th>Google Cloud Storage</th>
              <th>Microsoft Azure Blob Storage</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>비용</td>
              <td>저렴한 가격 대비 높은 성능</td>
              <td>저렴한 가격 대비 높은 성능</td>
              <td>비교적 높은 가격</td>
            </tr>
            <tr>
              <td>기능성</td>
              <td>높은 내구성 및 가용성</td>
              <td>고성능 및 확장성</td>
              <td>관리 작업 간소화 기능</td>
            </tr>
            <tr>
              <td>보안성</td>
              <td>액세스 제어, 객체 수준 암호화 및 다양한 암호화 방식 제공</td>
              <td>액세스 제어, 객체 수준 암호화 및 다양한 암호화 방식 제공</td>
              <td>액세스 제어, 객체 수준 암호화 및 다양한 암호화 방식 제공</td>
            </tr>
            <tr>
              <td>데이터 복제</td>
              <td>지리적으로 분산된 데이터 복제 가능</td>
              <td>지리적으로 분산된 데이터 복제 가능</td>
              <td>지리적으로 분산된 데이터 복제 가능</td>
            </tr>
            <tr>
              <td>사용 편의성</td>
              <td>AWS Management Console, CLI, SDK 등 다양한 도구를 사용한 구성 및 관리 가능</td>
              <td>RESTful API 및 CLI를 사용하여 쉽게 구성 및 관리 가능</td>
              <td>Azure Portal, PowerShell, CLI 등 다양한 도구를 사용하여 구성 및 관리 가능</td>
            </tr>
          </tbody>
        </table>
        </br>
        <ul>
          <li>AWS S3</li>
            <ul>
              <li>장점</li>
                <ul>
                  <li>저렴한 비용으로 매우 빠르고 안정적인 전송 속도와 데이터 신뢰성을 제공한다.</li>
                  <li>다양한 객체 타입을 지원하여 많은 종류의 데이터를 저장할 수 있다.</li>
                  <li>지리적으로 분산 된 데이터 복제 기능을 제공하여 데이터 손실을 방지한다.</li>
                  <li>AWS Management Console, CLI, SDK를 사용하여 구성 및 관리가 가능하다.</li>
                </ul>
              <li>단점</li>
                <ul>
                  <li>트래픽이 너무 많을 경우 비용이 높을 수 있다.</li>
                </ul>
            </ul>
          </br>
          <li>Google Cloud Storage</li>
            <ul>
              <li>장점</li>
                <ul>
                  <li>매우 빠르고 안정적인 전송 속도와 데이터 신뢰성을 제공한다.</li>
                  <li>저렴한 가격에 높은 성능을 제공한다.</li>
                  <li>지리적으로 분산 된 데이터 복제 기능을 제공하여 데이터 손실을 방지한다.</li>
                  <li>RESTful API와 CLI를 사용하여 쉽게 구성 및 관리할 수 있다.</li>
                </ul>
              <li>단점</li>
                <ul>
                  <li>AWS S3와 비교해 객체 타입 지원 범위가 제한적이다.</li>
                </ul>
            </ul>
          </br>
          <li>Microsoft Azure Blob Storage</li>
            <ul>
              <li>장점</li>
                <ul>
                  <li>관리 작업을 간소화하는 기능을 제공해서 관리 작업을 자동화할 수 있다.</li>
                  <li>암호화, 로그 기록 및 액세스 제어 등 다양한 보안 기능을 제공한다.</li>
                  <li>Azure Portal, PowerShell, CLI를 사용하여 구성 및 관리가 가능하다.</li>
                </ul>
              <li>단점</li>
                <ul>
                  <li>비교적으로 높은 가격으로 서비스를 제공한다.</li>
                </ul>
            </ul>
        </ul>
      </br>
      <li>최종결정</li>
        <ul>
          <li>S3, Google Cloud Storage, Microsoft Azure Blob Storage 세 가지 클라우드 스토리지 서비스는 성능, 기능 및 보안 측면에서 찰칵 프로젝트에 적용할 때에 큰 차이가 없다.</li>
          <li>찰칵 프로젝트는 AWS의 RDS, EC2, CloudFront와 같은 다양한 서비스를 사용하고 있어 비용 관리 측면에서 효율적으로 할 수 있고, 사용자 친화적인 API와 SDK를 제공하여 접근성이 높으며 상세하고 이해하기 쉽게 정리된 문서를 통해서 신속하게 필요한 정보를 찾고 문제를 해결할 수 있다는 점에서 S3를 선택하였다.</li>
        </ul>
    </ul>
    </br>
  </div>
</details>

<br>

<details>
  <summary><b>[CI/CD] GitHub Actions</b></summary>
  <div markdown="1">
    </br>
    <ul>
      <li>도입배경</li>
        <ul>
          <li>직접 테스트 코드를 실행하고 수동으로 배포를 하는 번거로움이 있었다. 개발 외에 소요되는 시간을 단축하고자 해당 과정을 자동화하기 위해 도입했다.</li>
        </ul>
      </br>
      <li>기술비교</li>
        <table width="70%">
          <thead>
            <tr>
              <th></th>
              <th>GitHub Actions</th>
              <th>Jenkins</th>
              <th>Travis CI</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>설치 및 설정</td>
              <td>GitHub 저장소 내 워크플로우 파일 작성으로 간단히 설정 가능</td>
              <td>별도의 서버와 플러그인 설치 필요</td>
              <td>GitHub 저장소와 연동하여 간단히 설정 가능</td>
            </tr>
            <tr>
              <td>비용</td>
              <td>무료 티어 범위(월별 2000분) 초과 시 유료</td>
              <td>서버 유지 비용 발생</td>
              <td>공개 저장소는 무료<br>비공개 저장소는 유료</td>
            </tr>
            <tr>
              <td>트리거 설정</td>
              <td>매우 유연</td>
              <td>매우 유연</td>
              <td>유연</td>
            </tr>
            <tr>
              <td>지속적 통합</td>
              <td>지원</td>
              <td>지원</td>
              <td>지원</td>
            </tr>
            <tr>
              <td>지속적 배포</td>
              <td>지원</td>
              <td>지원</td>
              <td>지원</td>
            </tr>
            <tr>
              <td>테스트 병렬화</td>
              <td>지원</td>
              <td>지원</td>
              <td>지원</td>
            </tr>
            <tr>
              <td>사용 언어</td>
              <td>YAML</td>
              <td>Java</td>
              <td>Ruby</td>
            </tr>
          </tbody>
        </table>
        </br>
        <ul>
          <li>GitHub Actions</li>
            <ul>
              <li>장점</li>
                <ul>
                  <li>실행을 트리거하는 방법으로 다양한 이벤트를 지원해 매우 유연한 트리거 설정이 가능하다.</li>
                  <li>일정 범위 내 무료로 이용이 가능하다.</li>
                  <li>GitHub와 연동이 원활하다.</li>
                </ul>
              <li>단점</li>
                <ul>
                  <li>특정 플러그인 및 확장이 부족하다.</li>
                  <li>다른 CI/CD 툴에 비해 사용자가 설정할 수 있는 옵션의 범위가 적다.</li>
                  <li>상대적으로 새로운 기술이기 때문에 자료가 부족하고 커뮤니티가 작다.</li>
                </ul>
            </ul>
          </br>
          <li>Jenkins</li>
            <ul>
              <li>장점</li>
                <ul>
                  <li>다양한 플러그인과 인터페이스를 지원한다.</li>
                  <li>1700개가 넘는 플러그인으로 폭이 넓은 사용자 설정을 할 수 있다.</li>
                  <li>매우 넓은 커뮤니티를 가지고 있어 자료 검색에 용이하다.</li>
                </ul>
              <li>단점</li>
                <ul>
                  <li>설정이 쉽지 않다.</li>
                  <li>보안 및 안정성 이슈가 발생할 수 있다.</li>
                  <li>대규모 프로젝트에서 많은 자원을 소비하고, 설정이 잘못되면 빌드가 느려질 수 있다.</li>
                </ul>
            </ul>
          </br>
          <li>Travis CI</li>
            <ul>
              <li>장점</li>
                <ul>
                  <li>설정이 간단하여 사용자가 쉽게 세팅할 수 있다.</li>
                  <li>단순한 빌드 프로세스에 적합하다.</li>
                  <li>커스텀 빌드 환경을 지원한다.</li>
                </ul>
              <li>단점</li>
                <ul>
                  <li>복잡한 빌드 파이프라인을 구성하기에는 상대적으로 제한적이다.</li>
                </ul>
            </ul>
        </ul>
      </br>
      <li>최종결정</li>
        <ul>
          <li>GitHub Actions는 GitHub에서 제공하는 서버리스 CI/CD 서버이기 때문에 Jenkins, Travis CI와 달리 호스팅이 따로 필요 없다. 그렇기 때문에 CI/CD 파이프라인을 실행하는 비용이 크게 감소한다.</li>
          <li>찰칵 프로젝트는 Git 원격 저장소를 GitHub로 사용하고 있어서 GitHub와의 연동이 매우 원활한 데다가 무료로 이용할 수 있으며 접근성이 좋은 GitHub Actions를 이용해 CI/CD를 도입하는 것으로 결정했다.</li>
        </ul>
    </ul>
  </br>
  </div>
</details>

<br>
<br>


## 🗂 프로젝트 구조

> 디렉토리 구조 들어갈 것

<br>
<br>

## 🛠 설계

<details>
  <summary><b>ERD</b></summary>
  <div markdown="1">
    <ul>
      <div></div>
    </ul>
  </div>
</details>

<details>
  <summary><b>API</b></summary>
  <div markdown="1">
    <ul>
      <div></div>
    </ul>
  </div>
</details>

<br>
<br>

## 🕵️‍♂️ 트러블슈팅

> 트러블슈팅 하나씩 채워 나갈 것것

<br>
<br>

## 😭 이번 프로젝트에서 보완해야 할 점

<br>
<br>

## 🤭 이번 프로젝트를 하며 새롭게 배운 것

<br>
<br>
