# Starlight Server
대학생 IT경영학회 큐시즘 32기 밋업 프로젝트 4조 Starlight 백엔드 레포지토리
<img width="1920" height="1080" alt="1" src="https://github.com/user-attachments/assets/92433169-8775-4808-b787-6c534a25cae9" />

<br></br>

## 👬 Member
|      정성호     |         이호근         |                                                                                                   
| :------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------------------------------------------------------------------------: | 
|   <img src="https://avatars.githubusercontent.com/SeongHo5356?v=4" width=120px alt="정성호"/>       |   <img src="https://avatars.githubusercontent.com/2ghrms?v=4" width=120px alt="이호근"/>                       |
|   [@SeongHo5356](https://github.com/SeongHo5356)   |    [@2ghrms](https://github.com/2ghrms)  | 

<br></br>

## 📝 Technology Stack
| Category             | Technology                                                                 |
|----------------------|---------------------------------------------------------------------------|
| **Language**         | Java 21                                                                 |
| **Framework**        | Spring Boot 3.3.10                                                        |
| **Databases**        | Postgresql, Redis                                                             |
| **Authentication**   | JWT, Spring Security, OAuth2.0                                           |
| **Development Tools**| Lombok                                                   |
| **API Documentation**| Swagger UI (SpringDoc)                                                   |
| **Storage**          | AWS S3, Naver Object Storage                                                                   |
| **Infrastructure**   | Terraform, NCP Server, HashiCorp Vault  |
| **Build Tools**      | Gradle    |
| **Monitoring** | Prometheus, Grafana, Loki, Promtail |

<br></br>

## 📅 ERD
https://www.erdcloud.com/d/bEeEkcvDoau3kf7W5
<img width="4240" height="1582" alt="스타라이트 ERD 제출본 (1)" src="https://github.com/user-attachments/assets/d90f822b-0df4-42fd-8f83-25e849e4e629" />

<br></br>

## 🔨 Project Architecture
<img width="5480" height="3640" alt="image (2)" src="https://github.com/user-attachments/assets/3af67db9-c1d1-4dec-9817-a39d0f57dc9e" />

<br></br>

## ⭐️ 기술스택/선정이유

**1️⃣ Java 21**

- Java 21은 최신 언어 기능(예: 패턴 매칭, 레코드, 가상 스레드 등)을 제공하여 코드의 가독성과 유지보수성을 높이며, 개발 생산성을 향상시킵니다.
- 최신 버전의 자바는 성능 최적화와 효율적인 메모리 관리 기능이 개선되어, 대규모 애플리케이션에서도 안정적이고 빠른 실행이 가능합니다.
- 장기 지원 버전이므로, 앞으로의 유지보수와 안정성 측면에서 신뢰할 수 있는 기반을 제공합니다.

**2️⃣ SpringBoot 3.4.9**
- 클라우드 네이티브 최적화: Jakarta EE 기반의 높은 성숙도를 갖추고 있으며, 헬스체크·Actuator·Micrometer 등 컨테이너 및 클라우드 운영에 필수적인 관측성 기능을 기본으로 제공합니다.
- 성능 및 보안 강화: 개선된 AOT/Native 이미지 지원을 통해 콜드스타트를 최적화할 수 있으며, 신속한 보안 패치로 안정적인 서비스 운영을 돕습니다.
- 생산성 및 유지보수: 표준화된 스타터와 강력한 자동 설정(Auto Configuration)으로 개발 복잡도를 낮추고, 팀 온보딩 및 유지보수 비용을 최소화합니다.

**3️⃣ SpringData JPA**

- Spring Data JPA는 데이터베이스와의 인터랙션을 단순화하고, 불필요한 보일러플레이트 코드를 줄여 개발 효율성을 높여줍니다.

**4️⃣ Spring AI**
- LLM 통합 단순화: Spring 생태계에 통합된 AI 프레임워크로, 복잡한 LLM 연동 과정을 간소화합니다.
- 유연한 추상화 레이어: OpenAI, Pinecone 등 다양한 제공업체에 대한 추상화 레이어를 제공하여 벤더 교체가 용이합니다.
- 높은 생산성: Spring Boot 자동 설정 및 WebFlux를 지원하며, 벡터 검색·프롬프트 템플릿·체이닝 등 RAG 패턴을 쉽게 구현할 수 있습니다.

**5️⃣ MySQL**
- 검증된 안정성: 성숙한 InnoDB 엔진과 풍부한 운영 레퍼런스를 보유하고 있으며, TCO(총 소유 비용)가 낮습니다.
- 기능 및 생태계: 8.x 버전의 CTE와 윈도우 함수로 복잡한 쿼리에 대응 가능하며, 리플리케이션·백업·모니터링 도구가 잘 갖춰져 있습니다.
- OLTP 최적화: HikariCP 커넥션 풀과 적절한 인덱싱을 통해 대규모 트래픽에서도 안정적인 운영이 가능합니다.

**6️⃣ NCP (CLOVA Studio, Server, Object Storage)**
- 안정적 인프라: 네이버 클라우드 플랫폼(NCP)의 서버 인프라와 Object Storage를 활용하여 안정적이고 보안성이 뛰어난 클라우드 환경을 제공합니다.
- AI 서비스 연동: CLOVA Studio와의 연동을 통해 프로젝트의 AI 기능을 효율적으로 지원하고 운영 효율성을 높입니다.

**7️⃣ ArgoCD, K3s**
- ArgoCD (GitOps): Pull 기반 배포로 'Git 상태=배포 상태'를 보장하며, 자동 동기화 및 자가 치유(Self-healing)로 운영 복잡도를 줄입니다.
- K3s (경량 K8s): 리소스가 제한된 환경이나 스테이징/소규모 서비스에 최적화된 가벼운 쿠버네티스입니다.
- 표준 호환성: Helm, Ingress 등 표준 K8s 도구를 그대로 사용할 수 있어 진입 장벽이 낮고, 추후 매니지드 서비스로의 확장이 쉽습니다.

**8️⃣ Promtail, Loki, Prometheus, Grafana**
- 로그 수집 및 검색 (Loki): Promtail이 수집한 애플리케이션 및 시스템 로그를 Loki로 전송하여 대용량 로그를 효율적으로 인덱싱하고 검색합니다.
- 메트릭 모니터링 (Prometheus): Pull 방식을 통해 API 응답 시간, CPU, 메모리 등의 시계열 데이터를 수집합니다.
- 데이터 시각화 (Grafana): 수집된 로그와 메트릭 데이터를 통합 대시보드로 시각화하여 시스템 상태를 한눈에 파악합니다.

**9️⃣ Flannel + WireGuard VPN**
- 네트워크 연결성: 서로 다른 네트워크(VPC, IDC, 공인망)에 분산된 노드 간의 통신을 지원합니다.
- 보안 통신 보장: K3s 클러스터 내에서 WireGuard VPN을 통해 빠르고 안전한 Pod-to-Pod 보안 통신을 보장합니다.

**🔟 Nginx**
- 고성능 리버스 프록시: 경량화된 구조로 TLS 종료, 정적 자산 서빙, 라우팅 및 리라이트 처리에 강점을 가집니다.
- 백엔드 보호: 헬스체크, 타임아웃, 버퍼링 등의 세밀한 튜닝을 통해 백엔드 애플리케이션의 부하를 줄이고 성능을 보장합니다.
- 유연한 배치: 쿠버네티스 환경에서 Ingress Controller 또는 엣지 프록시로 활용하여 트래픽을 효율적으로 관리합니다.

**1️⃣1️⃣ K6**
- 사용자 흐름 테스트: JavaScript를 사용하여 실제 사용자 행동(로그인 → API 호출 → 스케줄링)을 스크립트화하고 REST API 성능을 측정합니다.
- 커스텀 시나리오: vus(가상 사용자), stages 등의 옵션으로 스파이크 테스트나 지속성 테스트 등 다양한 부하 시나리오를 검증합니다.
- 결과 시각화: 응답 시간, 처리량, 에러율 등의 메트릭을 수집하고 Grafana와 연동하여 테스트 결과를 시각적으로 분석합니다.


<br></br>

## 💬 Convention

**commit convention** <br>
`#이슈번호 conventionType: 구현한 내용` <br><br>


**convention Type** <br>
| convention type | description |
| --- | --- |
| `feat` | 새로운 기능 구현 |
| `chore` | 부수적인 코드 수정 및 기타 변경사항 |
| `docs` | 문서 추가 및 수정, 삭제 |
| `fix` | 버그 수정 |
| `test` | 테스트 코드 추가 및 수정, 삭제 |
| `refactor` | 코드 리팩토링 |

<br></br>

## 🪵 Branch
### 
- `컨벤션명/#이슈번호-작업내용`
- pull request를 통해 develop branch에 merge 후, branch delete
- 부득이하게 develop branch에 직접 commit 해야 할 경우, `!hotfix:` 사용

<br></br>

## 📁 Directory

```PlainText
src/
├── main/
│   ├── domain/
│   │   ├── entity/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   └── dto/
            ├── request/
            └── response/
│   ├── global/
│   │   ├── apiPayload/
│   │   ├── config/
│   │   ├── security/
		 
```

<br></br>

## 📈  부하테스트
각 플랫폼(Instagram, Facebook, Threads) API에는 계정/시간 당 발행 가능한 게시물 수에 제한이 있어, 부하 테스트에는 제약이 존재합니다. 이에 따라 저희는 즉시 발행이 아닌 **"예약 발행" API**를 활용한 부하 시뮬레이션 방식을 구성하였습니다.

|시나리오 ① 10명이 1초 동안 최대한의 요청을 보낸다.| 시나리오 ② 2000명이 1초 동안 최대한의 요청을 보낸다.|
| :-------| :-------|
|![image](https://github.com/user-attachments/assets/026eb04b-4aa3-4e23-8820-5d22f1d94d12)|![image](https://github.com/user-attachments/assets/b73b7838-48e6-4392-99cd-c6497a4958d1)|
|✅ 총 120개의 요청이 문제없이 처리됨 <br>  - 평균 요청 처리 시간 : 82.09 ms <br>  - 최소 요청 처리 시간 : 22.52ms <br>  - 최대 요청 처리 시간 : 164.64ms |✅ 총 4002개의 요청이 문제없이 처리됨<br> - 평균 요청 처리 시간 : 7.74s <br>  - 최소 요청 처리 시간 : 21.9s <br>  - 최대 요청 처리 시간 : 18.28s <br> - 95th 퍼센타일 : 14.95s|

<br>

| 시나리오 ③ 사용자 수 변동 시나리오 | 시나리오 ④ 응답 시간이 5초 이내인 최대 요청 수 파악|
| :-------|:----|
|![image](https://github.com/user-attachments/assets/c77e54f8-765f-4ef5-a79b-f8896eb761a7)|![image](https://github.com/user-attachments/assets/a856af66-9d1b-47df-b287-156c125bd9b3)|
|0초 ~ 2초 : `50명`, 2초 ~ 12초 : `300명`, 12초 ~ 17초 : `1000명`, 17초 ~ 18초 : `500명`| 5초가 지날 경우 사용자 이탈이 늘어날 것이라고 판단하여 1초 동안 `1000명`의 사용자가 요청을 보내 `요청 처리 시간이 5초 이내`인 요청 개수를 파악 |
|✅ 총 3789개의 요청이 문제없이 처리됨 <br>  - 평균 요청 처리 시간 : 1.94s <br>  - 최소 요청 처리 시간 : 20.53ms <br>  - 최대 요청 처리 시간 : 7.88s |✅ 총 2002개의 요청이 시간 내 처리됨 |

### 테스트 결과 분석
- 현재 시스템은 동시 약 `1,000건` 수준까지는 안정적으로 요청을 처리할 수 있는 것으로 보입니다. **시나리오 ③**처럼 사용자 수가 점차 증가하는 상황에서도 평균 응답 시간은 `1.94초`, 최대 응답 시간은 `7.88초`로, 대부분의 요청이 정상적으로 처리되었습니다.
- 하지만 **시나리오 ②**처럼 `2,000명`의 동시 요청이 들어오면 평균 응답 시간이 `7.74초`, 최대 `18.28초`까지 증가하면서 응답 지연이 발생하였습니다. 이 결과는 대규모 트래픽에 대한 성능 한계가 있음을 보여주며, 추후 이를 개선할 필요가 있습니다.
- **시나리오 ④**에서는 `1000명`의 사용자가 동시에 요청을 보낸 경우, 총 `2,002건`의 요청이 `5초` 이내에 처리되었습니다. 이는 현재 시스템이 실시간 대응보다는 예약 처리에 더 적합한 구조임을 보여줍니다.
- 일반적으로 사용자 이탈이 늘기 시작하는 5초 이내 응답을 기준으로 예상 접속자 수 약 `1,000명` 정도에 대해서는 충분히 안정적인 성능을 제공할 수 있다고 판단됩니다.
