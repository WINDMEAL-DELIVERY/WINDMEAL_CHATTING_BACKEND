# WINDMEAL_CHATTING_BACKEND

## Conventions

### commit convention

`type`: **subject**

| 제목 및 태그 이름 | 설명 |
| --- | --- |
| Feat | 새로운 기능을 추가할 경우 |
| Fix | 버그를 고친 경우 |
| Design | CSS 등 사용자 UI 디자인 변경 |
| !BREAKING CHANGE | 커다란 API 변경의 경우 |
| !HOTFIX | 급하게 치명적인 버그를 고쳐야하는 경우 |
| Style | 코드 포맷 변경, 세미 콜론 누락, 코드 수정이 없는 경우 |
| Refactor | 프로덕션 코드 리팩토링 |
| Comment | 필요한 주석 추가 및 변경 |
| Docs | 문서를 수정한 경우 |
| Test | 테스트 추가, 테스트 리팩토링 (프로덕션 코드 변경 X) |
| Chore | 빌드 테스트 업데이트, 패키지 매니저를 설정하는 경우 (프로덕션 코드 변경 X) |
| Rename | 파일 혹은 폴더명을 수정하거나 옮기는 작업만인 경우 |
| Remove | 파일을 삭제하는 작업만 수행한 경우 |

`body` (optional)

`footer` (optional)

***

#### Commit Convention 에시
```
Feat: 추가 로그인 함수

로그인 API 개발

Resolves: #123
Ref: #456
Related to: #48, #45
```
***

# PR convention

## PR 타입

ex)
- [ ] 기능 추가
- [ ] 기능 삭제
- [ ] 기능 수정
- [x] 의존성, 환경 변수, 빌드 관련 코드 업데이트

## 작업사항
- ex) 로그인 시, 구글 소셜 로그인 기능을 추가했습니다. + commit 태그
- ex) 회원가입 로직 수정했습니다.
